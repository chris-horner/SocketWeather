package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.Loading
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.LocationFailed
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.NetworkFailed
import codes.chrishorner.socketweather.data.ForecastState.LoadingStatus.Success
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
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
    val refreshRequests = flowOf(Unit)

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations, refreshRequests)
    val states = mutableListOf<ForecastState>()
    val job = launch { forecaster.observeForecasts().collect { states.add(it) } }

    // The first state emitted should be `Loading`.
    assert(states[0].loadingStatus == Loading)
    assert(states[0].forecast == null)

    // The second state emitted should be `Success` with a non-null Forecast.
    assert(states[1].loadingStatus == Success)
    assert(states[1].forecast != null)

    job.cancel()
  }

  @Test fun `FollowMe location updates produce new forecasts`() = runBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)
    val deviceLocationChannel = ConflatedBroadcastChannel(DeviceLocation(1.0, 1.0))
    val refreshRequests = flowOf(Unit)

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocationChannel.asFlow(), refreshRequests)
    val states = mutableListOf<ForecastState>()
    val job = launch { forecaster.observeForecasts().collect { states.add(it) } }

    // The first sequence of updates should be
    // Loading -> Loading -> Success
    // Determining the location -> Retrieving the forecast -> Success
    assert(states[0].loadingStatus == Loading && states[0].location == null)
    assert(states[1].loadingStatus == Loading && states[1].location == testApi.location1)
    assert(states[2].loadingStatus == Success && states[2].forecast != null)

    // Next we pretend to be the device providing an updated location.
    deviceLocationChannel.send(DeviceLocation(2.0, 2.0))

    // The second sequence of updates should be
    // Loading -> Success
    // Retrieving the forecast -> Success
    assert(states[3].loadingStatus == Loading && states[3].location == testApi.location2)
    assert(states[4].loadingStatus == Success && states[4].forecast != null)

    job.cancel()
  }

  @Test fun `refresh requests produce new forecasts`() = runBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()
    val refreshRequestChannel = ConflatedBroadcastChannel(Unit)

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations, refreshRequestChannel.asFlow())
    val states = mutableListOf<ForecastState>()
    val job = launch { forecaster.observeForecasts().collect { states.add(it) } }

    // Initially transition from
    // Loading -> Success
    assert(states[0].loadingStatus == Loading && states[0].forecast == null)
    assert(states[1].loadingStatus == Success && states[1].forecast != null)

    refreshRequestChannel.send(Unit)

    // After a refresh request we should again transition from
    // Loading -> Success
    // However along with `Loading` it should contain a non-null Forecast.
    assert(states[2].loadingStatus == Loading && states[2].forecast != null)
    assert(states[3].loadingStatus == Success && states[3].forecast != null)

    job.cancel()
  }

  @Test fun `selecting different locations produces new forecasts`() = runBlockingTest {

    val selectionChannel = ConflatedBroadcastChannel(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()
    val refreshRequests = flowOf(Unit)

    val forecaster = Forecaster(fixedClock, testApi, selectionChannel.asFlow(), deviceLocations, refreshRequests)
    val states = mutableListOf<ForecastState>()
    val job = launch { forecaster.observeForecasts().collect { states.add(it) } }

    // Initially transition from
    // Loading -> Success
    assert(states[0].loadingStatus == Loading)
    assert(states[1].loadingStatus == Success && states[1].location == testApi.location1)

    selectionChannel.send(LocationSelection.Static(testApi.location2))

    // After selecting a different location, we should against transition from
    // Loading -> Success
    // Additionally, we verify that `Loading` shouldn't contain a cached Forecast.
    assert(states[2].loadingStatus == Loading && states[2].forecast == null)
    assert(states[3].loadingStatus == Success && states[3].location == testApi.location2)

    job.cancel()
  }

  @Test fun `device location errors produce error states`() = runBlockingTest {

    val selections = flowOf(LocationSelection.FollowMe)
    val refreshRequestChannel = ConflatedBroadcastChannel(Unit)

    var failDeviceLocation = true
    val deviceLocations = flow { if (failDeviceLocation) throw RuntimeException() else emit(DeviceLocation(1.0, 1.0)) }

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations, refreshRequestChannel.asFlow())
    val states = mutableListOf<ForecastState>()
    val job = launch { forecaster.observeForecasts().collect { states.add(it) } }

    // Because our DeviceLocation stream fails, we should transition from
    // Loading -> LocationFailed
    assert(states[0].loadingStatus == Loading)
    assert(states[1].loadingStatus == LocationFailed)

    failDeviceLocation = false
    refreshRequestChannel.send(Unit)

    // Now that DeviceLocation should succeed, after requesting a refresh our states should be
    // Loading location -> Loading forecast -> Success
    assert(states[2].loadingStatus == Loading)
    assert(states[3].loadingStatus == Loading)
    assert(states[4].loadingStatus == Success)

    job.cancel()
  }

  @Test fun `network errors produce error states`() = runBlockingTest {

    val selections = flowOf(LocationSelection.Static(testApi.location1))
    val deviceLocations = emptyFlow<DeviceLocation>()
    val refreshRequestChannel = ConflatedBroadcastChannel(Unit)

    testApi.failRequests(true)

    val forecaster = Forecaster(fixedClock, testApi, selections, deviceLocations, refreshRequestChannel.asFlow())
    val states = mutableListOf<ForecastState>()
    val job = launch { forecaster.observeForecasts().collect { states.add(it) } }

    // With network requests failing, our states should transition from
    // Loading -> NetworkFailed
    assert(states[0].loadingStatus == Loading)
    assert(states[1].loadingStatus == NetworkFailed)

    testApi.failRequests(false)
    refreshRequestChannel.send(Unit)

    // With network requests passing, a refresh request should transition us from
    // Loading -> Success
    assert(states[2].loadingStatus == Loading)
    assert(states[3].loadingStatus == Success)

    job.cancel()
  }
}
