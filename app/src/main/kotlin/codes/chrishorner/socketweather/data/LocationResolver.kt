package codes.chrishorner.socketweather.data

import android.app.Application
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Build
import androidx.core.content.getSystemService
import codes.chrishorner.socketweather.data.LocationResolver.Result
import codes.chrishorner.socketweather.data.LocationResolver.Result.Failure
import codes.chrishorner.socketweather.data.LocationResolver.Result.Success
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.location.Location as DeviceLocation

interface LocationResolver {

  suspend fun getDeviceLocation(): Result

  sealed class Result {
    data class Success(val location: Location) : Result()
    data class Failure(val type: ForecastError) : Result()
  }
}

private val australiaLatitudeRange = -44.057002..-9.763686
private val australiaLongitudeRange = 112.169980..154.927992

class RealLocationResolover(
  private val app: Application,
  private val api: WeatherApi,
) : LocationResolver {

  @Suppress("DEPRECATION") // Uses new API when possible.
  override suspend fun getDeviceLocation(): Result {
    val locationManager: LocationManager? = app.getSystemService()

    if (locationManager == null) {
      Timber.e("LocationManager not available.")
      return Failure(ForecastError.LOCATION)
    }

    val deviceLocation: DeviceLocation? = suspendCoroutine { cont ->
      val callback = LocationListener { location: DeviceLocation -> cont.resume(location) }

      try {
        if (Build.VERSION.SDK_INT >= 30) {
          locationManager.getCurrentLocation(NETWORK_PROVIDER, null, app.mainExecutor) { location ->
            callback.onLocationChanged(location)
          }
        } else {
          locationManager.requestSingleUpdate(NETWORK_PROVIDER, callback, null)
        }
      } catch (e: SecurityException) {
        Timber.e(e, "Location permission not granted.")
        cont.resume(null)
      }
    }

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
    } catch (e: Exception) {
      Timber.e(e, "Failed to resolve location with BOM.")
      Failure(ForecastError.NETWORK)
    }
  }
}
