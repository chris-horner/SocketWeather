package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context
import android.location.Location
import android.os.Looper
import codes.chrishorner.socketweather.util.arePlayServicesAvailable
import codes.chrishorner.socketweather.util.await
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
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

private val request = LocationRequest()
  .setPriority(PRIORITY_HIGH_ACCURACY)
  .setFastestInterval(1000)
  .setMaxWaitTime(3000)
  .setInterval(2000)
  .setSmallestDisplacement(50f)

private var cachedLocation: DeviceLocation? = null

private fun getDeviceLocationUpdates(context: Context): Flow<DeviceLocation> {
  if (!arePlayServicesAvailable(context)) return emptyFlow()

  val client = LocationServices.getFusedLocationProviderClient(context)

  return channelFlow {
    cachedLocation?.let { offer(it) }

    val lastKnownLocation: Location? = try {
      client.lastLocation.await()
    } catch (e: SecurityException) {
      Timber.e(e, "Location permission not granted.")
      null
    }

    if (lastKnownLocation != null && !isClosedForSend) {
      val deviceLocation = DeviceLocation(lastKnownLocation.latitude, lastKnownLocation.longitude)
      Timber.d("Found last known location: %f, %f", deviceLocation.latitude, deviceLocation.longitude)
      cachedLocation = deviceLocation
      send(deviceLocation)
    }

    val callback = object : LocationCallback() {

      override fun onLocationResult(result: LocationResult) {
        val deviceLocation = DeviceLocation(result.lastLocation.latitude, result.lastLocation.longitude)
        Timber.d("New location update: %f, %f", deviceLocation.latitude, deviceLocation.longitude)
        cachedLocation = deviceLocation
        offer(deviceLocation)
      }

      override fun onLocationAvailability(availability: LocationAvailability) {}
    }

    if (!isClosedForSend) {
      try {
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
      } catch (e: SecurityException) {
        Timber.e(e, "Location permission not granted.")
      }
    }

    awaitClose { client.removeLocationUpdates(callback) }
  }
}
