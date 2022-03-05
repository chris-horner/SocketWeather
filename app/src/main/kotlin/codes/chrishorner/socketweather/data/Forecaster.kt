package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.Forecaster.LoadingState
import codes.chrishorner.socketweather.data.Forecaster.LoadingState.Idle
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import java.time.Clock
import java.time.Instant

/**
 * A singleton that holds the current state of the [Forecast] for the whole application.
 */
interface Forecaster {

  sealed class LoadingState(open val selection: LocationSelection) {
    object Idle : LoadingState(LocationSelection.None)
    data class FindingLocation(override val selection: LocationSelection) : LoadingState(selection)
    data class LoadingForecast(override val selection: LocationSelection, val location: Location) :
      LoadingState(selection)

    data class Loaded(override val selection: LocationSelection, val forecast: Forecast) : LoadingState(selection)
    data class Refreshing(override val selection: LocationSelection, val previousForecast: Forecast) :
      LoadingState(selection)

    data class Error(override val selection: LocationSelection, val type: ForecastError) : LoadingState(selection)
  }

  val states: StateFlow<LoadingState>
  val forecast: StateFlow<Forecast?>
  fun refresh()

  object Crash : Forecaster {
    override val states: StateFlow<LoadingState>
      get() = TODO("Not yet implemented")
    override val forecast: StateFlow<Forecast?>
      get() = TODO("Not yet implemented")

    override fun refresh() {
      TODO("Not yet implemented")
    }
  }
}

class RealForecaster(
  clock: Clock,
  api: WeatherApi,
  locationSelections: Flow<LocationSelection>,
  deviceLocations: Flow<DeviceLocation>,
  scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
) : Forecaster {

  private val refreshes = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  private val forecastState = MutableStateFlow<Forecast?>(null)

  override val states: StateFlow<LoadingState>
  override val forecast: StateFlow<Forecast?> = forecastState

  init {
    val loadingStateFlow: Flow<LoadingState> = createFlowOfStates(
      clock,
      api,
      locationSelections,
      deviceLocations,
      refreshes.onStart { emit(Unit) }
    )

    states = loadingStateFlow.stateIn(scope, started = SharingStarted.Eagerly, Idle)

    scope.launch {
      states.filterIsInstance<LoadingState.Loaded>().collect { state ->
        forecastState.value = state.forecast
      }
    }
  }

  override fun refresh() {
    refreshes.tryEmit(Unit)
  }
}

private fun createFlowOfStates(
  clock: Clock,
  api: WeatherApi,
  locationSelections: Flow<LocationSelection>,
  deviceLocations: Flow<DeviceLocation>,
  refreshRequests: Flow<Unit>
): Flow<LoadingState> {
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
          emit(LoadingState.Refreshing(locationState.selection, lastKnownForecast))
        } else {
          emit(LoadingState.FindingLocation(locationState.selection))
        }
      }

      is LocationResolution.DeviceLocationError -> {
        cachedForecast = null
        emit(LoadingState.Error(locationState.selection, type = ForecastError.LOCATION))
      }

      is LocationResolution.NetworkError -> {
        cachedForecast = null
        emit(LoadingState.Error(locationState.selection, type = ForecastError.NETWORK))
      }

      is LocationResolution.NotInAustralia -> {
        cachedForecast = null
        emit(LoadingState.Error(locationState.selection, type = ForecastError.NOT_AUSTRALIA))
      }

      is LocationResolution.Resolved -> {
        if (lastKnownForecast != null) {
          emit(LoadingState.Refreshing(locationState.selection, lastKnownForecast))
        } else {
          emit(LoadingState.LoadingForecast(locationState.selection, locationState.location))
        }

        try {
          val forecast = loadForecast(api, clock, locationState.location)
          cachedForecast = forecast
          emit(LoadingState.Loaded(locationState.selection, forecast))
        } catch (e: JsonDataException) {
          Timber.e(e, "API returned unexpected data.")
          emit(LoadingState.Error(locationState.selection, ForecastError.DATA))
        } catch (e: Exception) {
          Timber.e(e, "Failed to load forecast.")
          emit(LoadingState.Error(locationState.selection, ForecastError.NETWORK))
        }
      }
    }
  }
}

private suspend fun loadForecast(api: WeatherApi, clock: Clock, location: Location): Forecast = supervisorScope {
  // Request observations, date, and hourly forecasts simultaneously.
  // For whatever reason for _some_ requests require that the `geohash` passed in is the
  // first 6 characters. Super annoying, but that's the price of an undocumented API.
  val observationsRequest = async { api.getObservations(location.geohash.take(6)) }
  val dateForecastsRequest = async { api.getDateForecasts(location.geohash.take(6)) }
  val hourlyForecastsRequest = async { api.getThreeHourlyForecasts(location.geohash.take(6)) }
  val observations: CurrentObservations = observationsRequest.await()
  val dateForecasts: List<DateForecast> = dateForecastsRequest.await()
  val hourlyForecasts: List<ThreeHourlyForecast> = hourlyForecastsRequest.await()

  val currentInfo: CurrentInformation = dateForecasts.getOrNull(0)?.now
    ?: throw JsonDataException("Invalid dateForecasts. First element must contain a valid 'now' field.")

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
