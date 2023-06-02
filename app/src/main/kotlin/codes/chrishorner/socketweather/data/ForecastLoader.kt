package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.ForecastLoader.Result
import codes.chrishorner.socketweather.data.ForecastLoader.State
import codes.chrishorner.socketweather.data.LocationSelection.FollowMe
import codes.chrishorner.socketweather.data.LocationSelection.None
import codes.chrishorner.socketweather.data.LocationSelection.Static
import codes.chrishorner.socketweather.widget.ForecastWidgetUpdater
import com.squareup.moshi.JsonDataException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Clock
import java.time.Duration
import java.time.Instant

interface ForecastLoader {

  val states: StateFlow<State>
  fun forceRefresh()
  fun refreshIfNecessary()
  suspend fun synchronousRefresh(): Result

  sealed class State {
    object Idle : State()
    object FindingLocation : State()
    object LoadingForecast : State()
    data class Error(val type: ForecastError) : State()
  }

  sealed class Result {
    object Success : Result()
    data class Failure(val type: ForecastError) : Result()
  }
}

class RealForecastLoader(
  private val clock: Clock,
  private val api: WeatherApi,
  private val locationResolver: LocationResolver,
  private val forecastStore: Store<Forecast?>,
  private val locationSelectionStore: Store<LocationSelection>,
  private val forecastWidgetUpdater: ForecastWidgetUpdater,
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) : ForecastLoader {

  private val statesFlow = MutableStateFlow<State>(State.Idle)
  private var refreshJob: Job? = null
  private val stateMutex = Mutex()

  override val states: StateFlow<State> = statesFlow

  override suspend fun synchronousRefresh(): Result = stateMutex.withLock {
    val location = when (val locationSelection = locationSelectionStore.data.value) {
      // No location selected yet. Give up.
      None -> return Result.Success
      // Easy. Grab the static selected location.
      is Static -> locationSelection.location
      // Update the state to FindingLocation and attempt to resolve it.
      FollowMe -> {
        statesFlow.value = State.FindingLocation

        when (val locationResult = locationResolver.getDeviceLocation()) {
          // If we fail to resolve the location, give up.
          is LocationResolver.Result.Failure -> {
            statesFlow.value = State.Error(locationResult.type)
            return Result.Failure(locationResult.type)
          }
          is LocationResolver.Result.Success -> locationResult.location
        }
      }
    }

    statesFlow.value = State.LoadingForecast

    return try {
      val forecast = loadForecast(api, clock, location)
      forecastStore.set(forecast)
      forecastWidgetUpdater.update()
      statesFlow.value = State.Idle
      Result.Success
    } catch (e: JsonDataException) {
      Timber.e(e, "API returned unexpected data.")
      statesFlow.value = State.Error(ForecastError.DATA)
      Result.Failure(ForecastError.DATA)
    } catch (e: Exception) {
      Timber.e(e, "Failed to load forecast.")
      statesFlow.value = State.Error(ForecastError.NETWORK)
      Result.Failure(ForecastError.NETWORK)
    }
  }

  override fun forceRefresh() {
    refreshJob?.cancel()
    refreshJob = scope.launch {
      synchronousRefresh()
    }
  }

  override fun refreshIfNecessary() {

    if (states.value != State.Idle) return

    val current = forecastStore.data.value

    if (current == null) {
      forceRefresh()
      return
    }

    val elapsedTime = Duration.between(current.updateTime, Instant.now(clock))
    if (elapsedTime.toMinutes() >= 1) {
      forceRefresh()
    }
  }
}

private suspend fun loadForecast(api: WeatherApi, clock: Clock, location: Location): Forecast = supervisorScope {
  // Request observations, date, and hourly forecasts simultaneously.
  // For whatever reason for _some_ requests require that the `geohash` passed in is the
  // first 6 characters. Super annoying, but that's the price of an undocumented API.
  val observationsRequest = async { api.getObservations(location.geohash.take(6)) }
  val dateForecastsRequest = async { api.getDateForecasts(location.geohash.take(6)) }
  val hourlyForecastsRequest = async { api.getHourlyForecasts(location.geohash.take(6)) }
  val observations: CurrentObservations = observationsRequest.await()
  val dateForecasts: List<DateForecast> = dateForecastsRequest.await()
  val hourlyForecasts: List<HourlyForecast> = hourlyForecastsRequest.await()
  val threeHourlyForecasts = hourlyForecasts.filterIndexed { index, _ -> index % 3 == 0 }

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
    hourlyForecasts = threeHourlyForecasts,
    upcomingForecasts = upcomingForecasts
  )
}
