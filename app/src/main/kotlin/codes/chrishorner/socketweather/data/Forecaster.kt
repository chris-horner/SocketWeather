package codes.chrishorner.socketweather.data

import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.Forecaster.State
import codes.chrishorner.socketweather.data.Forecaster.State.ErrorType
import codes.chrishorner.socketweather.data.Forecaster.State.Idle
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.supervisorScope
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import timber.log.Timber

/**
 * A singleton that holds the current state of the [Forecast] for the whole application.
 */
class Forecaster(
    clock: Clock,
    api: WeatherApi,
    locationSelections: Flow<LocationSelection>,
    deviceLocations: Flow<DeviceLocation>
) {

  sealed class State(open val selection: LocationSelection) {
    object Idle : State(LocationSelection.None)
    data class FindingLocation(override val selection: LocationSelection) : State(selection)
    data class LoadingForecast(override val selection: LocationSelection, val location: Location) : State(selection)
    data class Loaded(override val selection: LocationSelection, val forecast: Forecast) : State(selection)
    data class Refreshing(override val selection: LocationSelection, val previousForecast: Forecast) : State(selection)
    data class Error(override val selection: LocationSelection, val type: ErrorType) : State(selection)

    enum class ErrorType { DATA, NETWORK, LOCATION, NOT_AUSTRALIA }
  }

  private val states = MutableStateFlow<State>(Idle)
  private val refreshes = BroadcastChannel<Unit>(1)

  val currentState: State
    get() = states.value

  init {
    val stateFlow: Flow<State> = createStateFlow(
        clock,
        api,
        locationSelections,
        deviceLocations,
        refreshes.asFlow().onStart { emit(Unit) }
    )

    // Potentially tidier in the future: https://github.com/Kotlin/kotlinx.coroutines/issues/2047
    stateFlow
        .onEach { states.value = it }
        .launchIn(CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate))
  }

  @MainThread
  fun observeState(): Flow<State> = states

  fun refresh() {
    refreshes.offer(Unit)
  }
}

private fun createStateFlow(
    clock: Clock,
    api: WeatherApi,
    locationSelections: Flow<LocationSelection>,
    deviceLocations: Flow<DeviceLocation>,
    refreshRequests: Flow<Unit>
): Flow<State> {
  // Emit a LocationSelection whenever a new selection is made, or whenever a
  // new refresh request comes through.
  val selectionTriggers: Flow<LocationSelection> =
      combine(locationSelections, refreshRequests) { selection, _ -> selection }

  val locationResolutions = resolveLocations(selectionTriggers, deviceLocations, api)

  var cachedForecast: Forecast? = null

  return locationResolutions.transformLatest { locationState ->

    val lastKnownForecast: Forecast? = cachedForecast

    when (locationState) {
      is LocationResolution.Searching -> {
        if (lastKnownForecast != null) {
          emit(State.Refreshing(locationState.selection, lastKnownForecast))
        } else {
          emit(State.FindingLocation(locationState.selection))
        }
      }

      is LocationResolution.DeviceLocationError -> {
        cachedForecast = null
        emit(State.Error(locationState.selection, type = ErrorType.LOCATION))
      }

      is LocationResolution.NetworkError -> {
        cachedForecast = null
        emit(State.Error(locationState.selection, type = ErrorType.NETWORK))
      }

      is LocationResolution.NotInAustralia -> {
        cachedForecast = null
        emit(State.Error(locationState.selection, type = ErrorType.NOT_AUSTRALIA))
      }

      is LocationResolution.Resolved -> {
        if (lastKnownForecast != null) {
          emit(State.Refreshing(locationState.selection, lastKnownForecast))
        } else {
          emit(State.LoadingForecast(locationState.selection, locationState.location))
        }

        try {
          val forecast = loadForecast(api, clock, locationState.location)
          cachedForecast = forecast
          emit(State.Loaded(locationState.selection, forecast))
        } catch (e: JsonDataException) {
          Timber.e(e, "API returned unexpected data.")
          emit(State.Error(locationState.selection, ErrorType.DATA))
        } catch (e: Exception) {
          Timber.e(e, "Failed to load forecast.")
          emit(State.Error(locationState.selection, ErrorType.NETWORK))
        }
      }
    }
  }
}

private suspend fun loadForecast(api: WeatherApi, clock: Clock, location: Location): Forecast = supervisorScope {
  // Request observations, date, and hourly forecasts simultaneously.
  // For whatever reason for _some_ requests require that the `geohash` passed in is the
  // first 6 characters. Super annoying, but that's the price of an undocumentated API.
  val observationsRequest = async { api.getObservations(location.geohash.take(6)) }
  val dateForecastsRequest = async { api.getDateForecasts(location.geohash.take(6)) }
  val hourlyForecastsRequest = async { api.getThreeHourlyForecasts(location.geohash.take(6)) }
  val observations: CurrentObservations = observationsRequest.await()
  val dateForecasts: List<DateForecast> = dateForecastsRequest.await()
  val hourlyForecasts: List<ThreeHourlyForecast> = hourlyForecastsRequest.await()

  val currentInfo: CurrentInformation = requireNotNull(dateForecasts.getOrNull(0)?.now) {
    "Invalid dateForecasts. First element must contain a valid 'now' field."
  }

  val todayForecast: DateForecast = dateForecasts[0]

  // Determining the lowest temperature for the current time is a bit weird. There's
  // probably a better way to do this, but the API we're using is currently undocumented!
  val lowTemp = todayForecast.temp_min ?: if (currentInfo.now_label == "Max") {
    currentInfo.temp_later
  } else {
    currentInfo.temp_now
  }

  // Returned `DateForecasts` always include today as the first element in the list.
  // To get just the upcoming DateForecasts, we create a copy without the first element.
  val upcomingForecasts: List<DateForecast> = dateForecasts.drop(1)

  return@supervisorScope Forecast(
      updateTime = Instant.now(clock),
      location = location,
      iconDescriptor = todayForecast.icon_descriptor,
      night = currentInfo.is_night,
      currentTemp = observations.temp,
      tempFeelsLike = observations.temp_feels_like,
      humidity = observations.humidity,
      wind = observations.wind,
      highTemp = todayForecast.temp_max,
      lowTemp = lowTemp,
      todayForecast = todayForecast,
      hourlyForecasts = hourlyForecasts,
      upcomingForecasts = upcomingForecasts
  )
}
