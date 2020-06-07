package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.Forecaster.State.Error
import codes.chrishorner.socketweather.data.Forecaster.State.ErrorType
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
    val states = forecaster.observeState().test(this)

    // The emitted state should be `Loaded`.
    assertThat(states[0]).isInstanceOf<Loaded>()
    states.dispose()
  }

  @Test fun `FollowMe location updates produce new forecasts`() = runBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)
    val deviceLocations = MutableStateFlow(testApi.deviceLocation1)

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    forecaster.refresh()
    val states = forecaster.observeState().test(this)

    // Initially we should be displaying `Success` with location1's forecast.
    assertThat(states[0]).isInstanceOf<Loaded>()
    val initialState = states[0] as Loaded
    assertThat(initialState.forecast.location).isEqualTo(testApi.location1)

    // Next we pretend to be the device providing an updated location.
    deviceLocations.value = testApi.deviceLocation2

    // This should kick us into a `Refreshing` status.
    assertThat(states[1]).isInstanceOf<Refreshing>()
    val refreshingState = states[1] as Refreshing
    assertThat(refreshingState.previousForecast.location).isEqualTo(testApi.location1)

    // Once the forecast is loaded, we should be displaying `Success` again.
    assertThat(states[2]).isInstanceOf<Loaded>()
    val secondLoadedState = states[2] as Loaded
    assertThat(secondLoadedState.forecast.location).isEqualTo(testApi.location2)

    states.dispose()
  }

  @Test fun `refresh requests cause refresh to happen`() = runBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    forecaster.refresh()
    val states = forecaster.observeState().test(this)

    // Initially display `Loaded`.
    assertThat(states[0]).isInstanceOf<Loaded>()

    // Next we request a refresh.
    forecaster.refresh()

    // When refreshing, we should transition from `Refreshing` -> `Loaded`.
    assertThat(states[1]).isInstanceOf<Refreshing>()
    assertThat(states[2]).isInstanceOf<Loaded>()

    states.dispose()
  }

  @Test fun `selecting different locations produces new forecasts`() = runBlockingTest {

    val locationSelections = MutableStateFlow(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()

    val forecaster = Forecaster(fixedClock, testApi, locationSelections, deviceLocations)
    forecaster.refresh()
    val states = forecaster.observeState().test(this)

    // Initially we should be displaying `Loaded` with location1's forecast.
    assertThat(states[0]).isInstanceOf<Loaded>()
    val initialState = states[0] as Loaded
    assertThat(initialState.forecast.location).isEqualTo(testApi.location1)

    // Next we pretend to be the user selecting a different location.
    locationSelections.value = LocationSelection.Static(testApi.location2)

    // When loading the new location, we should transition from `Refreshing` -> `Loaded`.
    assertThat(states[1]).isInstanceOf<Refreshing>()
    val refreshingState = states[1] as Refreshing
    assertThat(refreshingState.previousForecast.location).isEqualTo(testApi.location1)
    assertThat(states[2]).isInstanceOf<Loaded>()
    val secondLoadedState = states[2] as Loaded
    assertThat(secondLoadedState.forecast.location).isEqualTo(testApi.location2)

    states.dispose()
  }

  @Test fun `device location errors produce error states`() = runBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)

    // Initially configure device location updates to fail.
    var failDeviceLocation = true
    val deviceLocations = flow { if (failDeviceLocation) throw RuntimeException() else emit(testApi.deviceLocation1) }

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    forecaster.refresh()
    val states = forecaster.observeState().test(this)

    // Our first state should be failure.
    assertThat(states[0]).isInstanceOf<Error>()
    val errorState = states[0] as Error
    assertThat(errorState.type).isEqualTo(ErrorType.LOCATION)

    // Next, reconfigure location updates to succeed and request a refresh.
    @Suppress("UNUSED_VALUE") // It's used in the flow { } block above.
    failDeviceLocation = false
    forecaster.refresh()

    // When refreshing, we should transition from `FindingLocation` -> `LoadingForecast` -> `Loaded`.
    assertThat(states[1]).isInstanceOf<FindingLocation>()
    assertThat(states[2]).isInstanceOf<LoadingForecast>()
    assertThat(states[3]).isInstanceOf<Loaded>()

    states.dispose()
  }

  @Test fun `network errors produce error states`() = runBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()

    // Initially configure network requests to fail.
    testApi.responseMode = ResponseMode.NETWORK_ERROR

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    forecaster.refresh()
    val states = forecaster.observeState().test(this)

    // With network requests failing, our initial state should be `Error` with type `NETWORK`.
    assertThat(states[0]).isInstanceOf<Error>()
    val errorState = states[0] as Error
    assertThat(errorState.type).isEqualTo(ErrorType.NETWORK)

    // Next, reconfigure network requests to succeed and request a refresh.
    testApi.responseMode = ResponseMode.SUCCESS
    forecaster.refresh()

    // When refreshing, we should transition from `LoadingForecast` -> `Loaded`.
    assertThat(states[1]).isInstanceOf<LoadingForecast>()
    assertThat(states[2]).isInstanceOf<Loaded>()

    states.dispose()
  }

  @Test fun `device location not in Australia produces error`() = runBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)
    // Pretend the device is in Tokyo.
    val deviceLocations = flowOf(DeviceLocation(35.680349, 139.769060))

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    forecaster.refresh()
    val states = forecaster.observeState().test(this)

    // The state emitted should be `Error` with type `NOT_AUSTRALIA`.
    assertThat(states[0]).isInstanceOf<Error>()
    val errorState = states[0] as Error
    assertThat(errorState.type).isEqualTo(ErrorType.NOT_AUSTRALIA)
    states.dispose()
  }

  @Test fun `malformed response produces error`() = runBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()

    // Initially configure network requests to fail with a data error.
    testApi.responseMode = ResponseMode.DATA_ERROR

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations)
    forecaster.refresh()
    val states = forecaster.observeState().test(this)

    // With network requests failing, our initial state should be `Error` with type `DATA`.
    assertThat(states[0]).isInstanceOf<Error>()
    val errorState = states[0] as Error
    assertThat(errorState.type).isEqualTo(ErrorType.DATA)

    // Next, reconfigure network requests to succeed and request a refresh.
    testApi.responseMode = ResponseMode.SUCCESS
    forecaster.refresh()

    // When refreshing, we should transition from `LoadingForecast` -> `Loaded`.
    assertThat(states[1]).isInstanceOf<LoadingForecast>()
    assertThat(states[2]).isInstanceOf<Loaded>()

    states.dispose()
  }
}
