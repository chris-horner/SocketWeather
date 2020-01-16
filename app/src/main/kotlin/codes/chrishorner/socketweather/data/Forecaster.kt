package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.Loading
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.LocationFailed
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.NetworkFailed
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.Success
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.broadcastIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scanReduce
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.supervisorScope
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import timber.log.Timber

class Forecaster(
    clock: Clock,
    api: WeatherApi,
    locationSelections: Flow<LocationSelection>,
    deviceLocations: Flow<DeviceLocation>
) {

  private val stateChannel = ConflatedBroadcastChannel<ForecastState>()
  private val refreshChannel = ConflatedBroadcastChannel(Unit)

  init {
    val flow = observeForecastStates(clock, api, locationSelections, deviceLocations, refreshChannel.asFlow())
    flow.onEach { stateChannel.send(it) }.launchIn(MainScope())
  }

  fun observeForecasts(): Flow<ForecastState> = stateChannel.asFlow()

  fun refresh() {
    refreshChannel.offer(Unit)
  }
}

private fun observeForecastStates(
    clock: Clock,
    api: WeatherApi,
    locationSelections: Flow<LocationSelection>,
    deviceLocations: Flow<DeviceLocation>,
    refreshRequests: Flow<Unit>
): Flow<ForecastState> {
  // Emit a LocationSelection whenever a new selection is made, or whenever a
  // new refresh request comes through.
  val selectionTriggers: Flow<LocationSelection> =
      combine(locationSelections, refreshRequests) { selection, _ -> selection }

  // Whenever a device location is emitted, query the API to get the Location for
  // that particular lat/long.
  val followMeUpdates: Flow<Location> = deviceLocations
      .mapLatest { api.searchForLocation("${it.latitude},${it.longitude}") }
      .map { api.getLocation(it[0].geohash) }
      .distinctUntilChanged()

  // Every time a new LocationSelection is made, produce a stream of LocatingStates.
  // This means that if the user selects a static location, we emit a single Located
  // state. If they select FollowMe, we first emit Searching and then subsequent
  // Located states every time we receive a new DeviceLocation.
  val locatingStates: Flow<LocatingState> = selectionTriggers.flatMapLatest { selection ->
    when (selection) {
      is LocationSelection.Static -> flowOf(LocatingState.Located(selection, selection.location))
      LocationSelection.None -> flowOf(LocatingState.Error(selection))
      LocationSelection.FollowMe -> followMeUpdates
          .map<Location, LocatingState> { LocatingState.Located(selection, it) }
          .catch {
            Timber.e(it, "Location updates failed.")
            emit(LocatingState.Error(selection))
          }
          .onStart { emit(LocatingState.Searching) }
    }
  }

  // As LocatingStates change, transform them into corresponding streams of
  // ForecastState. Searching for a location means the LoadingStatus of
  // ForecastState will be Loading. Error will be LocationFailed. Located means new
  // network requests will be kicked off in order to get the forecast for the newly
  // located location.
  val forecastStates: Flow<ForecastState> = locatingStates.transformLatest { locatingState ->
    when (locatingState) {
      is LocatingState.Searching -> {
        emit(ForecastState(locatingState.selection, loadingStatus = Loading))
      }

      is LocatingState.Error -> {
        emit(ForecastState(locatingState.selection, loadingStatus = LocationFailed))
      }

      is LocatingState.Located -> {
        emit(ForecastState(locatingState.selection, locatingState.location, loadingStatus = Loading))

        try {
          val forecast = loadForecast(api, clock, locatingState.location)
          emit(ForecastState(locatingState.selection, locatingState.location, forecast, loadingStatus = Success))
        } catch (e: Exception) {
          emit(ForecastState(locatingState.selection, locatingState.location, loadingStatus = NetworkFailed))
        }
      }
    }
  }

  // Finally, as we emit forecast states, check what state we're changing from and to.
  // If we're changing from one state to another and would lose our forecast information,
  // check if we're presenting the same location. If we are, we can reuse our
  // previously calculated forecasts.
  return forecastStates.scanReduce { previousState, newState ->
    if (newState.location == previousState.location && newState.forecast == null) {
      newState.copy(forecast = previousState.forecast)
    } else {
      newState
    }
  }
}

private suspend fun loadForecast(api: WeatherApi, clock: Clock, location: Location): Forecast = supervisorScope {
  // Request observations and date forecasts simultaneously.
  val observationsRequest = async { api.getObservations(location.geohash) }
  val dateForecastsRequest = async { api.getDateForecasts(location.geohash) }
  val observations: CurrentObservations = observationsRequest.await()
  val dateForecasts: List<DateForecast> = dateForecastsRequest.await()

  val currentInfo: CurrentInformation = requireNotNull(dateForecasts.getOrNull(0)?.now) {
    "Invalid dateForecasts. First element must contain a valid 'now' field."
  }

  // Determining the lowest temperature for the current time is a bit weird. There's
  // probably a better way to do this, but the API we're using is currently undocumented!
  val lowTemp = dateForecasts[0].temp_min ?: if (currentInfo.now_label == "Max") {
    currentInfo.temp_later
  } else {
    currentInfo.temp_now
  }

  return@supervisorScope Forecast(
      updateTime = Instant.now(clock),
      location = location,
      iconDescriptor = dateForecasts[0].icon_descriptor,
      night = currentInfo.is_night,
      currentTemp = observations.temp,
      tempFeelsLike = observations.temp_feels_like,
      highTemp = dateForecasts[0].temp_max,
      lowTemp = lowTemp,
      dateForecasts = dateForecasts
  )
}

private sealed class LocatingState(open val selection: LocationSelection) {
  object Searching : LocatingState(LocationSelection.FollowMe)
  data class Error(override val selection: LocationSelection) : LocatingState(selection)
  data class Located(override val selection: LocationSelection, val location: Location) : LocatingState(selection)
}
