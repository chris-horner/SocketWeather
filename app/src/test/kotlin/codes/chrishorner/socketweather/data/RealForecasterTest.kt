package codes.chrishorner.socketweather.data

import app.cash.turbine.test
import codes.chrishorner.socketweather.data.Forecaster.State.Error
import codes.chrishorner.socketweather.data.Forecaster.State.FindingLocation
import codes.chrishorner.socketweather.data.Forecaster.State.Loaded
import codes.chrishorner.socketweather.data.Forecaster.State.LoadingForecast
import codes.chrishorner.socketweather.data.Forecaster.State.Refreshing
import codes.chrishorner.socketweather.data.TestApi.ResponseMode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class RealForecasterTest {

  private val fixedClock: Clock
  private val testApi: TestApi

  init {
    val fixedDateTime = LocalDateTime.of(2020, 1, 12, 9, 0)
    val fixedInstant = ZonedDateTime.of(fixedDateTime, ZoneId.of("Australia/Melbourne")).toInstant()
    fixedClock = Clock.fixed(fixedInstant, ZoneId.of("Australia/Melbourne"))
    testApi = TestApi(fixedClock)
  }

  @Test fun `static location selection produces forecast`() = runCancellingBlockingTest {

    val locationSelection = LocationSelection.Static(testApi.location1)
    val selections = flowOf(locationSelection)
    val deviceLocations = emptyFlow<DeviceLocation>()
    val forecaster = RealForecaster(fixedClock, testApi, selections, deviceLocations, scope = this)

    forecaster.states.test {
      assertThat(awaitItem()).isInstanceOf<Loaded>()
    }
  }

  @Test fun `FollowMe location updates produce new forecasts`() = runCancellingBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)
    val deviceLocations = MutableStateFlow(testApi.deviceLocation1)
    val forecaster = RealForecaster(fixedClock, testApi, selections, deviceLocations, scope = this)

    forecaster.states.test {
      // Initially we should be displaying `Loaded` with location1's forecast.
      assertThat(expectItemAs<Loaded>().forecast.location).isEqualTo(testApi.location1)

      // Next we pretend to be the device providing an updated location.
      deviceLocations.value = testApi.deviceLocation2

      // This should kick us into a `Refreshing` status, but still with the initial location.
      assertThat(expectItemAs<Refreshing>().previousForecast.location).isEqualTo(testApi.location1)

      // Once the forecast is loaded, we should be displaying `Loaded` again with the new location.
      assertThat(expectItemAs<Loaded>().forecast.location).isEqualTo(testApi.location2)
    }
  }

  @Test fun `refresh requests cause refresh to happen`() = runCancellingBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()
    val forecaster = RealForecaster(fixedClock, testApi, selections, deviceLocations, scope = this)

    forecaster.states.test {
      // Initially display `Loaded`.
      assertThat(awaitItem()).isInstanceOf<Loaded>()

      // Next we request a refresh.
      advanceUntilIdle()
      forecaster.refresh()

      // When refreshing, we should transition from `Refreshing` -> `Loaded`.
      assertThat(awaitItem()).isInstanceOf<Refreshing>()
      assertThat(awaitItem()).isInstanceOf<Loaded>()
    }
  }

  @Test fun `selecting different locations produces new forecasts`() = runCancellingBlockingTest {

    val locationSelections = MutableStateFlow(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()
    val forecaster = RealForecaster(fixedClock, testApi, locationSelections, deviceLocations, scope = this)

    forecaster.states.test {
      // Initially we should be displaying `Loaded` with location1's forecast.
      assertThat(expectItemAs<Loaded>().forecast.location).isEqualTo(testApi.location1)

      // Next we pretend to be the user selecting a different location.
      locationSelections.value = LocationSelection.Static(testApi.location2)

      // When loading the new location, we should transition from `Refreshing` -> `Loaded`.
      assertThat(expectItemAs<Refreshing>().previousForecast.location).isEqualTo(testApi.location1)
      assertThat(expectItemAs<Loaded>().forecast.location).isEqualTo(testApi.location2)
    }
  }

  @Test fun `device location errors produce error states`() = runCancellingBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)

    // Initially configure device location updates to fail.
    var failDeviceLocation = true
    val deviceLocations = flow { if (failDeviceLocation) throw RuntimeException() else emit(testApi.deviceLocation1) }

    val forecaster = RealForecaster(fixedClock, testApi, selections, deviceLocations, scope = this)

    forecaster.states.test {
      // Our first state should be failure.
      assertThat(expectItemAs<Error>().type).isEqualTo(ForecastError.LOCATION)

      // Next, reconfigure location updates to succeed and request a refresh.
      advanceUntilIdle()
      failDeviceLocation = false
      forecaster.refresh()

      // When refreshing, we should transition from `FindingLocation` -> `LoadingForecast` -> `Loaded`.
      assertThat(awaitItem()).isInstanceOf<FindingLocation>()
      assertThat(awaitItem()).isInstanceOf<LoadingForecast>()
      assertThat(awaitItem()).isInstanceOf<Loaded>()
    }
  }

  @Test fun `network errors produce error states`() = runCancellingBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()

    // Initially configure network requests to fail.
    testApi.responseMode = ResponseMode.NETWORK_ERROR

    val forecaster = RealForecaster(fixedClock, testApi, selections, deviceLocations, scope = this)

    forecaster.states.test {
      // With network requests failing, our initial state should be `Error` with type `NETWORK`.
      assertThat(expectItemAs<Error>().type).isEqualTo(ForecastError.NETWORK)

      // Next, reconfigure network requests to succeed and request a refresh.
      advanceUntilIdle()
      testApi.responseMode = ResponseMode.SUCCESS
      forecaster.refresh()

      // When refreshing, we should transition from `LoadingForecast` -> `Loaded`.
      assertThat(awaitItem()).isInstanceOf<LoadingForecast>()
      assertThat(awaitItem()).isInstanceOf<Loaded>()
    }
  }

  @Test fun `device location not in Australia produces error`() = runCancellingBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)
    // Pretend the device is in Tokyo.
    val deviceLocations = flowOf(DeviceLocation(35.680349, 139.769060))

    val forecaster = RealForecaster(fixedClock, testApi, selections, deviceLocations, scope = this)

    forecaster.states.test {
      // The state emitted should be `Error` with type `NOT_AUSTRALIA`.
      assertThat(expectItemAs<Error>().type).isEqualTo(ForecastError.NOT_AUSTRALIA)
    }
  }

  @Test fun `malformed response produces error`() = runCancellingBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()

    // Initially configure network requests to fail with a data error.
    testApi.responseMode = ResponseMode.DATA_ERROR

    val forecaster = RealForecaster(fixedClock, testApi, selections, deviceLocations, scope = this)

    forecaster.states.test {
      // With network requests failing, our initial state should be `Error` with type `DATA`.
      assertThat(expectItemAs<Error>().type).isEqualTo(ForecastError.DATA)

      // Next, reconfigure network requests to succeed and request a refresh.
      advanceUntilIdle()
      testApi.responseMode = ResponseMode.SUCCESS
      forecaster.refresh()

      // When refreshing, we should transition from `LoadingForecast` -> `Loaded`.
      assertThat(awaitItem()).isInstanceOf<LoadingForecast>()
      assertThat(awaitItem()).isInstanceOf<Loaded>()
    }
  }
}
