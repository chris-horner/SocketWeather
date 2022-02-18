package codes.chrishorner.socketweather.test

import codes.chrishorner.socketweather.data.LocationResolver
import codes.chrishorner.socketweather.data.LocationResolver.Result

class FakeLocationResolver : LocationResolver {

  val result = TestChannel<Result>()

  override suspend fun getDeviceLocation(): Result {
    return result.awaitValue()
  }
}
