package codes.chrishorner.socketweather.data

import app.cash.turbine.Turbine
import codes.chrishorner.socketweather.data.LocationResolver.Result
import codes.chrishorner.socketweather.test.FakeApi
import codes.chrishorner.socketweather.test.TestData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

class RealLocationResolverTest {

  private val locationResolver: RealLocationResolver
  private val api: FakeApi
  private val deviceLocator = FakeDeviceLocator()

  init {
    val time = ZonedDateTime.of(2022, 2, 19, 10, 0, 0, 0, ZoneId.of("Australia/Melbourne"))
    val clock = Clock.fixed(time.toInstant(), time.zone)
    api = FakeApi(clock)
    locationResolver = RealLocationResolver(deviceLocator, api)
  }

  @Test fun `valid device location produces location`() = runBlocking {
    deviceLocator.location.add(TestData.deviceLocation1)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Success(TestData.location1))
  }

  @Test fun `no device location produces error`() = runBlocking {
    deviceLocator.location.add(null)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Failure(ForecastError.LOCATION))
  }

  @Test fun `location outside australia produces error`() = runBlocking {
    val tokyo = DeviceLocation(35.659478998452336, 139.7005600428793)
    deviceLocator.location.add(tokyo)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Failure(ForecastError.NOT_AUSTRALIA))
  }

  @Test fun `malformed response produces error`() = runBlocking {
    api.responseMode = FakeApi.ResponseMode.DATA_ERROR
    deviceLocator.location.add(TestData.deviceLocation1)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Failure(ForecastError.DATA))
  }

  @Test fun `network failure produces error`() = runBlocking {
    api.responseMode = FakeApi.ResponseMode.NETWORK_ERROR
    deviceLocator.location.add(TestData.deviceLocation1)
    val result = locationResolver.getDeviceLocation()
    assertThat(result).isEqualTo(Result.Failure(ForecastError.NETWORK))
  }

  private class FakeDeviceLocator : DeviceLocator {

    val location = Turbine<DeviceLocation?>()

    override suspend fun getLocation(): DeviceLocation? {
      return location.awaitItem()
    }
  }
}
