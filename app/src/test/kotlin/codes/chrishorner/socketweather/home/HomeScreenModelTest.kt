package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.ForecastError
import codes.chrishorner.socketweather.data.ForecastLoader
import codes.chrishorner.socketweather.data.ForecastLoader.State
import codes.chrishorner.socketweather.data.Location
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.test.DefaultLocaleRule
import codes.chrishorner.socketweather.test.FakeStore
import codes.chrishorner.socketweather.test.FakeStrings
import codes.chrishorner.socketweather.test.MutableClock
import codes.chrishorner.socketweather.test.TestApi
import codes.chrishorner.socketweather.test.isInstanceOf
import codes.chrishorner.socketweather.test.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class HomeScreenModelTest {

  @get:Rule val localeRule = DefaultLocaleRule(Locale.forLanguageTag("en-AU"))

  private val forecastLoader = FakeForecastLoader()
  private val forecast = MutableStateFlow<Forecast?>(null)
  private val currentSelectionStore = FakeStore<LocationSelection>(LocationSelection.None)
  private val allSelections = MutableStateFlow<Set<LocationSelection>>(emptySet())
  private val clock: MutableClock
  private val screenModel: HomeScreenModel
  private val testApi: TestApi

  private val strings = FakeStrings(
    R.string.home_loading to "Loading forecast…",
    R.string.home_findingLocation to "Finding location…",
    R.string.home_updatingNow to "Refreshing…",
    R.string.home_lastUpdated to "Updated %s",
    R.string.home_justUpdated to "Updated just now",
    R.string.home_updatingNow to "Updating now…",
  )

  init {
    val startTime = ZonedDateTime.of(2022, 2, 27, 9, 0, 0, 0, ZoneId.of("Australia/Melbourne"))
    clock = MutableClock(startTime.toOffsetDateTime())
    testApi = TestApi(clock)
    screenModel = HomeScreenModel(forecastLoader, forecast, currentSelectionStore, allSelections, strings, clock)
  }

  @Test fun `null forecast with idle loading shows empty state`() {
    screenModel.test {
      val state = awaitItem()
      assertThat(state.content).isEqualTo(HomeState.Content.Empty)
    }
  }

  @Test fun `loaded forecast with idle loading shows loaded state`() = runBlocking {
    setTestDataWith(testApi.location1)

    screenModel.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo(testApi.location1.name)
        assertThat(toolbarSubtitle).isEqualTo("Updated just now")
        assertThat(currentLocation.selection).isEqualTo(LocationSelection.Static(testApi.location1))
        assertThat(savedLocations).isEmpty()
        assertThat(showRefreshingIndicator).isFalse()
        assertThat(content).isInstanceOf<HomeState.Content.Loaded>()
      }
    }
  }

  @Test fun `loaded forecast with loading show refreshing state`() = runBlocking {
    setTestDataWith(testApi.location1)
    forecastLoader.states.value = State.LoadingForecast

    screenModel.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo(testApi.location1.name)
        assertThat(toolbarSubtitle).isEqualTo("Updating now…")
        assertThat(showRefreshingIndicator).isEqualTo(true)
        assertThat(content).isInstanceOf<HomeState.Content.Loaded>()
      }
    }
  }

  @Test fun `null forecast with loading shows loading state`() = runBlocking {
    forecastLoader.states.value = State.LoadingForecast

    screenModel.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo("Loading forecast…")
        assertThat(toolbarSubtitle).isEqualTo("Updating now…")
        assertThat(showRefreshingIndicator).isEqualTo(false)
        assertThat(content).isEqualTo(HomeState.Content.Loading)
      }
    }
  }

  @Test fun `null forecast with error shows error state`() = runBlocking {
    forecastLoader.states.value = State.Error(ForecastError.NETWORK)

    screenModel.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo("Loading forecast…")
        assertThat(toolbarSubtitle).isNull()
        assertThat(content).isEqualTo(HomeState.Content.Error(ForecastError.NETWORK))
      }
    }
  }

  @Test fun `loaded forecast with error show error state`() = runBlocking {
    setTestDataWith(testApi.location1)
    forecastLoader.states.value = State.Error(ForecastError.NETWORK)

    screenModel.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo(testApi.location1.name)
        assertThat(toolbarSubtitle).isEqualTo("Updated just now")
        assertThat(content).isEqualTo(HomeState.Content.Error(ForecastError.NETWORK))
      }
    }
  }

  @Test fun `loaded forecast over a minute ago shows update subtitle`() = runBlocking {
    setTestDataWith(testApi.location1)
    clock.advanceBy(Duration.ofMinutes(1))

    screenModel.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo(testApi.location1.name)
        assertThat(toolbarSubtitle).isEqualTo("Updated Relative time string")
        assertThat(content).isInstanceOf<HomeState.Content.Loaded>()
      }
    }
  }

  private suspend fun setTestDataWith(location: Location) {
    val locationSelection = LocationSelection.Static(location)
    currentSelectionStore.set(locationSelection)
    allSelections.value = setOf(locationSelection)
    val forecastData = generateFakeForecast(location)
    forecast.value = forecastData
  }

  private suspend fun generateFakeForecast(location: Location): Forecast {

    val observations = testApi.getObservations(location.geohash)
    val dateForecasts = testApi.getDateForecasts(location.geohash)
    val hourlyForecasts = testApi.getThreeHourlyForecasts(location.geohash)
    val todayForecast = dateForecasts[0]

    return Forecast(
      updateTime = clock.instant(),
      location = location,
      iconDescriptor = todayForecast.icon_descriptor,
      night = todayForecast.now!!.is_night,
      currentTemp = observations.temp,
      tempFeelsLike = observations.temp_feels_like,
      humidity = observations.humidity,
      wind = observations.wind,
      highTemp = todayForecast.temp_max,
      lowTemp = todayForecast.temp_min!!,
      todayForecast = todayForecast,
      hourlyForecasts = hourlyForecasts,
      upcomingForecasts = dateForecasts,
    )
  }

  private class FakeForecastLoader : ForecastLoader {

    override val states = MutableStateFlow<State>(State.Idle)

    override fun refreshIfNecessary() = Unit

    override fun forceRefresh() = Unit
  }
}