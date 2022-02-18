package codes.chrishorner.socketweather.data

import app.cash.turbine.test
import codes.chrishorner.socketweather.data.ForecastLoader.State
import codes.chrishorner.socketweather.data.LocationResolver.Result
import codes.chrishorner.socketweather.test.FakeStore
import codes.chrishorner.socketweather.test.MutableClock
import codes.chrishorner.socketweather.test.TestApi
import codes.chrishorner.socketweather.test.TestChannel
import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

@RunWith(TestParameterInjector::class)
class RealForecastLoaderTest {

  private val locationResolver = FakeLocationResolver()
  private val forecastStore = FakeStore<Forecast?>(null)
  private val locationSelectionStore = FakeStore<LocationSelection>(LocationSelection.None)
  private val clock: MutableClock
  private val api: TestApi

  init {
    val startTime = ZonedDateTime.of(2022, 2, 19, 9, 0, 0, 0, ZoneId.of("Australia/Melbourne"))
    clock = MutableClock(startTime.toOffsetDateTime())
    api = TestApi(clock)
  }

  private fun create(scope: CoroutineScope) = RealForecastLoader(
    clock, api, locationResolver, forecastStore, locationSelectionStore, scope
  )

  @Test fun `successful refresh of LocationSelection-Static updates state and store`() = runBlocking {
    locationSelectionStore.set(LocationSelection.Static(api.location1))
    val forecastLoader = create(this)

    forecastLoader.states.test {
      assertThat(awaitItem()).isEqualTo(State.Idle)
      forecastLoader.forceRefresh()
      assertThat(awaitItem()).isEqualTo(State.LoadingForecast)
      assertThat(awaitItem()).isEqualTo(State.Idle)
    }

    // Verify that the forecast loaded matches the input test data.
    val forecast = forecastStore.data.value!!
    assertThat(forecast.location).isEqualTo(api.location1)
    assertThat(forecast.updateTime).isEqualTo(clock.instant())
  }

  @Test fun `successful refresh of LocationSelection-FollowMe updates state and store`() = runBlocking {
    locationSelectionStore.set(LocationSelection.FollowMe)
    locationResolver.result.send(Result.Success(api.location1))
    val forecastLoader = create(this)

    forecastLoader.states.test {
      assertThat(awaitItem()).isEqualTo(State.Idle)
      forecastLoader.forceRefresh()
      assertThat(awaitItem()).isEqualTo(State.FindingLocation)
      assertThat(awaitItem()).isEqualTo(State.LoadingForecast)
      assertThat(awaitItem()).isEqualTo(State.Idle)
    }

    // Verify that the forecast loaded matches the input test data.
    val forecast = forecastStore.data.value!!
    assertThat(forecast.location).isEqualTo(api.location1)
    assertThat(forecast.updateTime).isEqualTo(clock.instant())
  }

  @Test fun `loader only refreshes when forecast is stale`() = runBlocking {
    locationSelectionStore.set(LocationSelection.Static(api.location1))
    val forecastLoader = create(this)

    forecastLoader.states.test {
      // Start idle with no forecast.
      assertThat(awaitItem()).isEqualTo(State.Idle)
      assertThat(forecastStore.data.value).isNull()

      // Refreshing should update the forecast.
      forecastLoader.refreshIfNecessary()
      assertThat(awaitItem()).isEqualTo(State.LoadingForecast)
      assertThat(awaitItem()).isEqualTo(State.Idle)
      assertThat(forecastStore.data.value!!.updateTime).isEqualTo(clock.instant())

      // Refreshing again immediately should not update the forecast.
      forecastLoader.refreshIfNecessary()
      expectNoEvents()

      // Refreshing after 1 minute should update the forecast.
      clock.advanceBy(Duration.ofMinutes(1))
      forecastLoader.refreshIfNecessary()
      assertThat(awaitItem()).isEqualTo(State.LoadingForecast)
      assertThat(awaitItem()).isEqualTo(State.Idle)
      assertThat(forecastStore.data.value!!.updateTime).isEqualTo(clock.instant())
    }
  }

  @Test fun `location resolution failure produces error state`(@TestParameter type: ForecastError) = runBlocking {
    locationSelectionStore.set(LocationSelection.FollowMe)
    locationResolver.result.send(Result.Failure(type))
    val forecastLoader = create(this)

    forecastLoader.states.test {
      assertThat(awaitItem()).isEqualTo(State.Idle)
      forecastLoader.forceRefresh()
      assertThat(awaitItem()).isEqualTo(State.FindingLocation)
      assertThat(awaitItem()).isEqualTo(State.Error(type))
    }
  }

  @Test fun `network failure produces error state`() = runBlocking {
    locationSelectionStore.set(LocationSelection.Static(api.location1))
    api.responseMode = TestApi.ResponseMode.NETWORK_ERROR
    val forecastLoader = create(this)

    forecastLoader.states.test {
      assertThat(awaitItem()).isEqualTo(State.Idle)
      forecastLoader.forceRefresh()
      assertThat(awaitItem()).isEqualTo(State.LoadingForecast)
      assertThat(awaitItem()).isEqualTo(State.Error(ForecastError.NETWORK))
    }
  }

  @Test fun `malformed data produces error state`() = runBlocking {
    locationSelectionStore.set(LocationSelection.Static(api.location1))
    api.responseMode = TestApi.ResponseMode.DATA_ERROR
    val forecastLoader = create(this)

    forecastLoader.states.test {
      assertThat(awaitItem()).isEqualTo(State.Idle)
      forecastLoader.forceRefresh()
      assertThat(awaitItem()).isEqualTo(State.LoadingForecast)
      assertThat(awaitItem()).isEqualTo(State.Error(ForecastError.DATA))
    }
  }

  private class FakeLocationResolver : LocationResolver {

    val result = TestChannel<Result>()

    override suspend fun getDeviceLocation(): Result {
      return result.awaitValue()
    }
  }
}
