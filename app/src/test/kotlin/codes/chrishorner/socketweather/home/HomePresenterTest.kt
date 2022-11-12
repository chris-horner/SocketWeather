package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.about.AboutScreen
import codes.chrishorner.socketweather.choose_location.ChooseLocationScreen
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.ForecastError
import codes.chrishorner.socketweather.data.ForecastLoader.State
import codes.chrishorner.socketweather.data.Location
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.rain_radar.RainRadarScreen
import codes.chrishorner.socketweather.test.DefaultLocaleRule
import codes.chrishorner.socketweather.test.FakeForecastLoader
import codes.chrishorner.socketweather.test.FakeNavigator
import codes.chrishorner.socketweather.test.FakeStore
import codes.chrishorner.socketweather.test.FakeStrings
import codes.chrishorner.socketweather.test.MutableClock
import codes.chrishorner.socketweather.test.TestApi
import codes.chrishorner.socketweather.test.TestData
import codes.chrishorner.socketweather.test.containsExactlyInOrder
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

class HomePresenterTest {

  @get:Rule val localeRule = DefaultLocaleRule(Locale.forLanguageTag("en-AU"))

  private val navigator = FakeNavigator(HomeScreen)
  private val forecastLoader = FakeForecastLoader()
  private val forecast = MutableStateFlow<Forecast?>(null)
  private val currentSelectionStore = FakeStore<LocationSelection>(LocationSelection.None)
  private val allSelections = MutableStateFlow<Set<LocationSelection>>(emptySet())
  private val clock: MutableClock
  private val presenter: HomePresenter
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
    presenter = HomePresenter(
      navigator, forecastLoader, forecast, currentSelectionStore, allSelections, strings, clock
    )
  }

  @Test fun `null forecast with idle loading shows empty state`() = runBlocking {
    presenter.test {
      val state = awaitItem()
      assertThat(state.content).isEqualTo(HomeState.Content.Empty)
    }
  }

  @Test fun `loaded forecast with idle loading shows loaded state`() = runBlocking {
    setTestDataWith(TestData.location1)

    presenter.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo(TestData.location1.name)
        assertThat(toolbarSubtitle).isEqualTo("Updated just now")
        assertThat(currentLocation.selection).isEqualTo(LocationSelection.Static(TestData.location1))
        assertThat(savedLocations).isEmpty()
        assertThat(showRefreshingIndicator).isFalse()
        assertThat(content).isInstanceOf<HomeState.Content.Loaded>()
      }
    }
  }

  @Test fun `loaded forecast with loading show refreshing state`() = runBlocking {
    setTestDataWith(TestData.location1)
    forecastLoader.states.value = State.LoadingForecast

    presenter.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo(TestData.location1.name)
        assertThat(toolbarSubtitle).isEqualTo("Updating now…")
        assertThat(showRefreshingIndicator).isEqualTo(true)
        assertThat(content).isInstanceOf<HomeState.Content.Loaded>()
      }
    }
  }

  @Test fun `null forecast with loading shows loading state`() = runBlocking {
    forecastLoader.states.value = State.LoadingForecast

    presenter.test {
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

    presenter.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo("Loading forecast…")
        assertThat(toolbarSubtitle).isNull()
        assertThat(content).isEqualTo(HomeState.Content.Error(ForecastError.NETWORK))
      }
    }
  }

  @Test fun `loaded forecast with error show error state`() = runBlocking {
    setTestDataWith(TestData.location1)
    forecastLoader.states.value = State.Error(ForecastError.NETWORK)

    presenter.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo(TestData.location1.name)
        assertThat(toolbarSubtitle).isEqualTo("Updated just now")
        assertThat(content).isEqualTo(HomeState.Content.Error(ForecastError.NETWORK))
      }
    }
  }

  @Test fun `loaded forecast over a minute ago shows update subtitle`() = runBlocking {
    setTestDataWith(TestData.location1)
    clock.advanceBy(Duration.ofMinutes(1))

    presenter.test {
      with(awaitItem()) {
        assertThat(toolbarTitle).isEqualTo(TestData.location1.name)
        assertThat(toolbarSubtitle).isEqualTo("Updated Relative time string")
        assertThat(content).isInstanceOf<HomeState.Content.Loaded>()
      }
    }
  }

  @Test fun `AddLocation event navigates to ChooseLocationScreen`() = runBlocking {
    setTestDataWith(TestData.location1)

    presenter.test {
      awaitItem()
      sendEvent(HomeEvent.AddLocation)
      assertThat(navigator.awaitStackChange()).containsExactlyInOrder(
        HomeScreen,
        ChooseLocationScreen(showCloseButton = true),
      )
    }
  }

  @Test fun `Refresh event forces forecast to refresh`() = runBlocking {
    setTestDataWith(TestData.location1)

    presenter.test {
      awaitItem()
      sendEvent(HomeEvent.Refresh)
      forecastLoader.refreshCalls.awaitItem()
    }
  }

  @Test fun `SwitchLocation event changes selection and forces refresh`() = runBlocking {
    setTestDataWith(TestData.location1)

    presenter.test {
      awaitItem()
      assertThat(currentSelectionStore.data.value).isEqualTo(LocationSelection.Static(TestData.location1))
      sendEvent(HomeEvent.SwitchLocation(LocationSelection.Static(TestData.location2)))
      forecastLoader.refreshCalls.awaitItem()
      assertThat(currentSelectionStore.data.value).isEqualTo(LocationSelection.Static(TestData.location2))
      assertThat(awaitItem().currentLocation.selection).isEqualTo(LocationSelection.Static(TestData.location2))
    }
  }

  @Test fun `ViewAbout event navigates to AboutScreen`() = runBlocking {
    setTestDataWith(TestData.location1)

    presenter.test {
      awaitItem()
      sendEvent(HomeEvent.ViewAbout)
      assertThat(navigator.awaitStackChange()).containsExactlyInOrder(HomeScreen, AboutScreen)
    }
  }

  @Test fun `ViewRainRadar event navigates to RainRadarScreen`() = runBlocking {
    setTestDataWith(TestData.location1)

    presenter.test {
      awaitItem()
      sendEvent(HomeEvent.ViewRainRadar)
      assertThat(navigator.awaitStackChange()).containsExactlyInOrder(HomeScreen, RainRadarScreen)
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
}
