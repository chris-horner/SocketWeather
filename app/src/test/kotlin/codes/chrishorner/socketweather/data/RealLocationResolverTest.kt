package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.LocationResolver.Result
import codes.chrishorner.socketweather.test.TestApi
import codes.chrishorner.socketweather.test.TestChannel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class RealLocationResolverTest {

  private val locationResolver: RealLocationResolver
  private val api: TestApi
  private val deviceLocator = FakeDeviceLocator()

  init {
    val time = ZonedDateTime.of(2022, 2, 19, 10, 0, 0, 0, ZoneId.of("Australia/Melbourne"))
    val clock = Clock.fixed(time.toInstant(), time.zone)
    api = TestApi(clock)
    locationResolver = RealLocationResolver(deviceLocator, api)
  }

  @Test fun `valid device location produces location`() = runBlocking {
    deviceLocator.location.send(api.deviceLocation1)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Success(api.location1))
  }

  @Test fun `no device location produces error`() = runBlocking {
    deviceLocator.location.send(null)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Failure(ForecastError.LOCATION))
  }

  @Test fun `location outside australia produces error`() = runBlocking {
    val tokyo = DeviceLocation(35.659478998452336, 139.7005600428793)
    deviceLocator.location.send(tokyo)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Failure(ForecastError.NOT_AUSTRALIA))
  }

  @Test fun `malformed response produces error`() = runBlocking {
    api.responseMode = TestApi.ResponseMode.DATA_ERROR
    deviceLocator.location.send(api.deviceLocation1)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Failure(ForecastError.DATA))
  }

  @Test fun `network failure produces error`() = runBlocking {
    api.responseMode = TestApi.ResponseMode.NETWORK_ERROR
    deviceLocator.location.send(api.deviceLocation1)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Failure(ForecastError.NETWORK))
  }

  private class FakeDeviceLocator : DeviceLocator {

    val location = TestChannel<DeviceLocation?>()

    override suspend fun getLocation(): DeviceLocation? {
      return location.awaitValue()
    }
  }
}
