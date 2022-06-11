package codes.chrishorner.socketweather.data

import android.app.Application
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Build
import androidx.core.content.getSystemService
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface DeviceLocator {
  suspend fun getLocation(): DeviceLocation?
}

class AndroidDeviceLocator(
  private val app: Application,
  private val lastKnownLocation: Store<DeviceLocation?>,
) : DeviceLocator {

  @Suppress("DEPRECATION") // Uses new API when possible.
  override suspend fun getLocation(): DeviceLocation? {
    val locationManager: LocationManager? = app.getSystemService()

    if (locationManager == null) {
      Timber.e("LocationManager not available.")
      return null
    }

    val locationResult: Result<Location?> = suspendCoroutine { cont ->
      val callback = LocationListener { location: Location? ->
        // LocationManager.getCurrentLocation() and LocationManager.requestSingleUpdate() can
        // both return null if invoked while the app is in the background. This is still
        // treated as "success", and we fall back to the last known location.
        cont.resume(Result.success(location))
      }

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
        cont.resume(Result.failure(e))
      }
    }

    if (locationResult.isFailure) return null

    val deviceLocation = locationResult.getOrThrow()?.let { DeviceLocation(it.latitude, it.longitude) }
    if (deviceLocation != null) {
      lastKnownLocation.set(deviceLocation)
    } else {
      Timber.w("LocationManager delivered null location.")
    }
    return deviceLocation
  }
}
