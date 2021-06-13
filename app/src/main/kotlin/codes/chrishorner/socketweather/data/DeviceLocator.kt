package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.NETWORK_PROVIDER
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import timber.log.Timber

interface DeviceLocator {
  fun enable()
  fun disable()
  fun observeDeviceLocation(): Flow<DeviceLocation>
}

class RealDeviceLocator(private val app: Application) : DeviceLocator {

  private val enabledFlow = MutableStateFlow(false)

  override fun enable() {
    enabledFlow.value = true
  }

  override fun disable() {
    enabledFlow.value = false
  }

  override fun observeDeviceLocation(): Flow<DeviceLocation> {
    return enabledFlow
      .flatMapLatest { enabled -> if (enabled) getDeviceLocationUpdates(app) else emptyFlow() }
      .distinctUntilChanged()
      .conflate()
  }
}

private var cachedLocation: DeviceLocation? = null

private fun getDeviceLocationUpdates(context: Context): Flow<DeviceLocation> {
  val locationManager: LocationManager = context.getSystemService()!!

  return callbackFlow {
    cachedLocation?.let { send(it) }

    val lastKnownLocation = try {
      locationManager.getLastKnownLocation(NETWORK_PROVIDER)
    } catch (e: SecurityException) {
      Timber.e(e, "Location permission not granted.")
      close(e)
      null
    }

    if (lastKnownLocation != null && !isClosedForSend) {
      val deviceLocation = DeviceLocation(lastKnownLocation.latitude, lastKnownLocation.longitude)
      Timber.d("Found last known location: %f, %f", deviceLocation.latitude, deviceLocation.longitude)
      cachedLocation = deviceLocation
      send(deviceLocation)
    }

    val callback = object : LocationListener {
      override fun onLocationChanged(location: Location) {
        val deviceLocation = DeviceLocation(location.latitude, location.longitude)
        Timber.d("New location update: %f, %f", deviceLocation.latitude, deviceLocation.longitude)
        cachedLocation = deviceLocation
        if (!isClosedForSend) trySend(deviceLocation)
      }

      override fun onProviderEnabled(provider: String) {}

      override fun onProviderDisabled(provider: String) {
        Timber.e("Location provider %s isn't enabled.", provider)
        close(IllegalStateException("Location provider $provider isn't enabled."))
      }
    }

    try {
      locationManager.requestLocationUpdates(NETWORK_PROVIDER, 3000, 50f, callback)
    } catch (e: SecurityException) {
      Timber.e(e, "Location permission not granted.")
      close(e)
    }

    awaitClose { locationManager.removeUpdates(callback) }
  }
}
