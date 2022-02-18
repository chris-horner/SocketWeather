package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.LocationResolver.Result
import codes.chrishorner.socketweather.data.LocationResolver.Result.Failure
import codes.chrishorner.socketweather.data.LocationResolver.Result.Success
import com.squareup.moshi.JsonDataException
import timber.log.Timber

interface LocationResolver {

  suspend fun getDeviceLocation(): Result

  sealed class Result {
    data class Success(val location: Location) : Result()
    data class Failure(val type: ForecastError) : Result()
  }
}

private val australiaLatitudeRange = -44.057002..-9.763686
private val australiaLongitudeRange = 112.169980..154.927992

class RealLocationResolver(
  private val deviceLocator: DeviceLocator2,
  private val api: WeatherApi,
) : LocationResolver {

  override suspend fun getDeviceLocation(): Result {
    val deviceLocation = deviceLocator.getLocation()

    if (deviceLocation == null) {
      Timber.e("Failed to retrieve device location.")
      return Failure(ForecastError.LOCATION)
    }

    val latitude = deviceLocation.latitude
    val longitude = deviceLocation.longitude

    if (latitude !in australiaLatitudeRange || longitude !in australiaLongitudeRange) {
      return Failure(ForecastError.NOT_AUSTRALIA)
    }

    return try {
      val searchResults = api.searchForLocation("$latitude,$longitude")
      val location = api.getLocation(searchResults[0].geohash)
      Success(location)
    } catch (e: JsonDataException) {
      Timber.e(e, "Failed to resolve location due to malformed data.")
      Failure(ForecastError.DATA)
    } catch (e: Exception) {
      Timber.e(e, "Failed to resolve location with BOM.")
      Failure(ForecastError.NETWORK)
    }
  }
}
