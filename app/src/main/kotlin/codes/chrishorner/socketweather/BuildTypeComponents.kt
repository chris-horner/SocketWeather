package codes.chrishorner.socketweather

import android.app.Application
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DeviceLocation
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.Store

/**
 * Generators for things that change depending on the build type being debug or release.
 */
@MainThread
interface BuildTypeComponents {
  fun createDeviceLocator(app: Application, lastKnownLocation: Store<DeviceLocation?>): DeviceLocator
  fun createNetworkComponents(app: Application): NetworkComponents
}
