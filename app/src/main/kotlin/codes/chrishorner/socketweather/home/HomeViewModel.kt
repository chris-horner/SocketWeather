package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.data.CurrentInformation
import codes.chrishorner.socketweather.data.CurrentObservations
import codes.chrishorner.socketweather.data.DateForecast
import codes.chrishorner.socketweather.data.DeviceLocation
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.Location
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.WeatherApi
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.Loading
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.LocationFailed
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.NetworkFailed
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.Success
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scanReduce
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.supervisorScope
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import timber.log.Timber

class HomeViewModel(
    private val api: WeatherApi,
    private val clock: Clock,
    currentSelectionUpdates: Flow<LocationSelection>,
    deviceLocationUpdates: Flow<DeviceLocation>
) {

  private val scope = MainScope()
  private val refreshChannel = ConflatedBroadcastChannel(Unit)
  private val statesChannel = ConflatedBroadcastChannel<State>()
  // Whether or not a subscription to device location updates should be maintained.
  // This allows location updates to be disabled when the UI is not displayed.
  private val locationUpdatesToggleChannel = ConflatedBroadcastChannel(false)

  init {

    val followMeUpdates: Flow<Location> = locationUpdatesToggleChannel.asFlow()
        .flatMapLatest { enabled -> if (enabled) deviceLocationUpdates else emptyFlow() }
        .distinctUntilChanged()
        .mapLatest { api.searchForLocation("${it.latitude},${it.longitude}") }
        .map { api.getLocation(it[0].geohash) }
        .distinctUntilChanged()

    val selectionTriggers: Flow<LocationSelection> =
        combine(currentSelectionUpdates, refreshChannel.asFlow()) { selection, _ -> selection }

    val locationUpdates: Flow<LocationUpdate> = selectionTriggers
        .flatMapLatest { selection ->
          when (selection) {
            is LocationSelection.Static -> flowOf(LocationUpdate.Loaded(selection, selection.location))
            LocationSelection.None -> flowOf(LocationUpdate.Error(selection))
            LocationSelection.FollowMe -> followMeUpdates
                .map<Location, LocationUpdate> { LocationUpdate.Loaded(selection, it) }
                .catch {
                  Timber.e(it, "Location updates failed.")
                  emit(LocationUpdate.Error(selection))
                }
                .onStart { emit(LocationUpdate.Loading(selection)) }
          }
        }

    val states: Flow<State> = locationUpdates
        .transform { locationUpdate ->
          when (locationUpdate) {
            is LocationUpdate.Loading -> {
              emit(State(locationUpdate.selection, loadingStatus = Loading))
            }

            is LocationUpdate.Error -> {
              emit(State(locationUpdate.selection, loadingStatus = LocationFailed))
            }

            is LocationUpdate.Loaded -> {
              emit(State(locationUpdate.selection, currentLocation = locationUpdate.location, loadingStatus = Loading))
              emit(loadForecast(locationUpdate))
            }
          }
        }
        .scanReduce { previousState: State, newState: State ->
          // TODO: Replace this with some kind of repository for the current forecast.
          // If we're changing from one state to another and would lose our forecast information, check if we're
          // presenting the same location. If we are, we can reuse our previously calculated forecasts.
          if (newState.currentLocation == previousState.currentLocation && newState.forecast == null) {
            newState.copy(forecast = previousState.forecast)
          } else {
            newState
          }
        }

    states.onEach { statesChannel.offer(it) }.launchIn(scope)
  }

  fun enableLocationUpdates(enable: Boolean) {
    locationUpdatesToggleChannel.offer(enable)
  }

  fun observeStates(): Flow<State> = statesChannel.asFlow()

  fun forceRefresh() {
    refreshChannel.offer(Unit)
  }

  fun destroy() {
    scope.cancel()
  }

  private suspend fun loadForecast(locationUpdate: LocationUpdate.Loaded): State = supervisorScope {
    try {
      val geohash: String = locationUpdate.location.geohash
      // Request observations and date forecasts simultaneously.
      val observationsRequest = async { api.getObservations(geohash) }
      val dateForecastsRequest = async { api.getDateForecasts(geohash) }

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

      val forecast = Forecast(
          updateTime = Instant.now(clock),
          location = locationUpdate.location,
          iconDescriptor = dateForecasts[0].icon_descriptor,
          night = currentInfo.is_night,
          currentTemp = observations.temp,
          tempFeelsLike = observations.temp_feels_like,
          highTemp = dateForecasts[0].temp_max,
          lowTemp = lowTemp,
          dateForecasts = dateForecasts
      )

      return@supervisorScope State(
          currentSelection = locationUpdate.selection,
          currentLocation = locationUpdate.location,
          forecast = forecast,
          loadingStatus = Success
      )
    } catch (e: Exception) {
      Timber.e(e, "Failed to get forecasts for %s", locationUpdate.location.name)
      return@supervisorScope State(
          currentSelection = locationUpdate.selection,
          currentLocation = locationUpdate.location,
          loadingStatus = NetworkFailed
      )
    }
  }

  data class State(
      val currentSelection: LocationSelection,
      val currentLocation: Location? = null,
      val forecast: Forecast? = null,
      val loadingStatus: LoadingStatus = Loading
  )

  enum class LoadingStatus { Loading, LocationFailed, NetworkFailed, Success }

  private sealed class LocationUpdate(open val selection: LocationSelection) {
    data class Loading(override val selection: LocationSelection) : LocationUpdate(selection)
    data class Error(override val selection: LocationSelection) : LocationUpdate(selection)
    data class Loaded(override val selection: LocationSelection, val location: Location) : LocationUpdate(selection)
  }
}
