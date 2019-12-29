package codes.chrishorner.socketweather.data

import android.content.Context
import android.location.Location
import codes.chrishorner.socketweather.util.arePlayServicesAvailable
import codes.chrishorner.socketweather.util.await
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private val request = LocationRequest()
    .setPriority(PRIORITY_BALANCED_POWER_ACCURACY)
    .setFastestInterval(1000)
    .setSmallestDisplacement(100f)

suspend fun canGetLocation(context: Context): Boolean {
  if (!arePlayServicesAvailable(context)) return false

  val settingsRequest = LocationSettingsRequest.Builder().addLocationRequest(request).build()
  val settingsClient = LocationServices.getSettingsClient(context)
  val task = settingsClient.checkLocationSettings(settingsRequest)

  return suspendCancellableCoroutine { continuation ->
    task.addOnSuccessListener {
      if (continuation.isCancelled) continuation.cancel() else continuation.resume(true)
    }

    task.addOnFailureListener {
      if (continuation.isCancelled) continuation.cancel() else continuation.resume(false)
    }
  }
}

fun getLocationUpdates(context: Context): Flow<DeviceLocation> {
  if (!arePlayServicesAvailable(context)) return emptyFlow()

  val client = LocationServices.getFusedLocationProviderClient(context)

  return channelFlow {

    val lastKnownLocation: Location? = try {
      client.lastLocation.await()
    } catch (e: Exception) {
      null
    }

    lastKnownLocation?.let { send(DeviceLocation(it.latitude, it.longitude)) }

    val callback = object : LocationCallback() {

      override fun onLocationResult(result: LocationResult) {
        with(result.lastLocation) { offer(DeviceLocation(latitude, longitude)) }
      }

      override fun onLocationAvailability(availability: LocationAvailability) {}
    }

    client.requestLocationUpdates(request, callback, null)
    awaitClose { client.removeLocationUpdates(callback) }
  }
}
