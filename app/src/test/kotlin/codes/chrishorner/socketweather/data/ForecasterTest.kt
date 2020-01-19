package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.Loading
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.LocationFailed
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.NetworkFailed
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.Success
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class ForecasterTest {

  @Rule @JvmField val dispatcherRule = MainDispatcherRule()

  private val fixedClock: Clock
  private val testApi: TestApi

  init {
    val fixedDateTime = LocalDateTime.of(2020, 1, 12, 9, 0)
    val fixedInstant = ZonedDateTime.of(fixedDateTime, ZoneId.of("Australia/Melbourne")).toInstant()
    fixedClock = Clock.fixed(fixedInstant, ZoneId.of("Australia/Melbourne"))
    testApi = TestApi(fixedClock)
  }

  @Test fun `static location selection produces forecast`() = runBlockingTest {

    val locationSelection = LocationSelection.Static(testApi.location1)
    val selections = flowOf(locationSelection)
    val deviceLocations = emptyFlow<DeviceLocation>()

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    val states = forecaster.observeForecasts().test(this)

    // The emitted emitted state should be `Success` with a non-null forecast.
    assertThat(states[0].loadingStatus).isEqualTo(Success)
    assertThat(states[0].forecast).isNotNull()

    states.dispose()
  }

  @Test fun `FollowMe location updates produce new forecasts`() = runBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)
    val deviceLocationChannel = ConflatedBroadcastChannel(DeviceLocation(1.0, 1.0))

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocationChannel.asFlow())
    val states = forecaster.observeForecasts().test(this)

    // Initially we should be displaying `Success` with location1's forecast.
    assertThat(states[0].loadingStatus).isEqualTo(Success)
    assertThat(states[0].location).isEqualTo(testApi.location1)
    assertThat(states[0].forecast).isNotNull()

    // Next we pretend to be the device providing an updated location.
    deviceLocationChannel.send(DeviceLocation(2.0, 2.0))

    // This should kick us into a `Loading` status for location2.
    assertThat(states[1].loadingStatus).isEqualTo(Loading)
    assertThat(states[1].location).isEqualTo(testApi.location2)
    assertThat(states[1].forecast).isNull()

    // Once the forecast is loaded, we should be displaying `Success` again.
    assertThat(states[2].loadingStatus).isEqualTo(Success)
    assertThat(states[2].location).isEqualTo(testApi.location2)
    assertThat(states[2].forecast).isNotNull()

    states.dispose()
  }

  @Test fun `refresh requests produce new forecasts`() = runBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    val states = forecaster.observeForecasts().test(this)

    // Initially display `Success` with a non-null forecast.
    assertThat(states[0].loadingStatus).isEqualTo(Success)
    assertThat(states[0].forecast).isNotNull()

    // Next we request a refresh.
    forecaster.refresh()

    // When refreshing, we should transition from `Loading` -> `Success`.
    assertThat(states[1].loadingStatus).isEqualTo(Loading)
    assertThat(states[2].loadingStatus).isEqualTo(Success)

    // Also when refreshing, we should never have a null forecast since the location hasn't changed.
    assertThat(states[1].forecast).isNotNull()
    assertThat(states[2].forecast).isNotNull()

    states.dispose()
  }

  @Test fun `selecting different locations produces new forecasts`() = runBlockingTest {

    val selectionChannel = ConflatedBroadcastChannel(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()

    val forecaster = Forecaster(fixedClock, testApi, selectionChannel.asFlow(), deviceLocations)
    val states = forecaster.observeForecasts().test(this)

    // Initially we should be displaying `Success` with location1's forecast.
    assertThat(states[0].loadingStatus).isEqualTo(Success)
    assertThat(states[0].location).isEqualTo(testApi.location1)
    assertThat(states[0].forecast).isNotNull()

    // Next we pretend to be the user selecting a different location.
    selectionChannel.send(LocationSelection.Static(testApi.location2))

    // When loading the new location, we should transition from `Loading` -> `Success`.
    assertThat(states[1].loadingStatus).isEqualTo(Loading)
    assertThat(states[2].loadingStatus).isEqualTo(Success)

    // Also when loading the new location, the forecast should be reset to null since we're changing locations.
    assertThat(states[1].forecast).isNull()
    assertThat(states[2].forecast).isNotNull()

    states.dispose()
  }

  @Test fun `device location errors produce error states`() = runBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)

    // Initially configure device location updates to fail.
    var failDeviceLocation = true
    val deviceLocations = flow { if (failDeviceLocation) throw RuntimeException() else emit(DeviceLocation(1.0, 1.0)) }

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    val states = forecaster.observeForecasts().test(this)

    // Our first state should be failure.
    assertThat(states[0].loadingStatus).isEqualTo(LocationFailed)

    // Next, reconfigure location updates to succeed and request a refresh.
    failDeviceLocation = false
    forecaster.refresh()

    // When refreshing, we should transition from `Loading` -> `Loading` -> `Success`.
    // This is because we first load the location, then load the forecast, then display success.
    assertThat(states[1].loadingStatus).isEqualTo(Loading)
    assertThat(states[2].loadingStatus).isEqualTo(Loading)
    assertThat(states[3].loadingStatus).isEqualTo(Success)

    states.dispose()
  }

  @Test fun `network errors produce error states`() = runBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()

    // Initially configure network requests to fail.
    testApi.failRequests(true)

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    val states = forecaster.observeForecasts().test(this)

    // With network requests failing, our initial state should be `NetworkFailed`.
    assertThat(states[0].loadingStatus).isEqualTo(NetworkFailed)

    // Next, reconfigure network requests to succeed and request a refresh.
    testApi.failRequests(false)
    forecaster.refresh()

    // When refreshing, we should transition from `Loading` -> `Success`.
    assert(states[1].loadingStatus == Loading)
    assert(states[2].loadingStatus == Success)

    states.dispose()
  }

  @Test fun `refresh requests for same location keep forecast`() = runBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)
    val deviceLocationChannel = ConflatedBroadcastChannel(DeviceLocation(1.0, 1.0))

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocationChannel.asFlow())
    val states = forecaster.observeForecasts().test(this)

    // Initially we should be displaying `Success` with location1's forecast.
    assertThat(states[0].loadingStatus).isEqualTo(Success)

    // Request a refresh.
    forecaster.refresh()

    // The next state emitted should have a status of `Loading` and a non-null forecast,
    // since our location hasn't changed.
    assertThat(states[1].loadingStatus).isEqualTo(Loading)
    assertThat(states[1].forecast).isNotNull()

    states.dispose()
  }
}
