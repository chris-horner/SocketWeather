package codes.chrishorner.socketweather.data

import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.create

class MockWeatherApi(mockRetrofit: MockRetrofit) : WeatherApi {

  private val delegate: BehaviorDelegate<WeatherApi> = mockRetrofit.create()

  private val mockLocation = Location(
      id = "mockLocation",
      geohash = "mockLocation",
      name = "Mocksville",
      postcode = "MOCK",
      state = "VIC"
  )

  override suspend fun searchForLocation(query: String): List<Location> {
    return delegate.returningResponse(listOf(mockLocation)).searchForLocation(query)
  }

  override suspend fun searchForLocation(latitude: Double, longitude: Double): List<Location> {
    return delegate.returningResponse(mockLocation).searchForLocation(latitude, longitude)
  }
}
