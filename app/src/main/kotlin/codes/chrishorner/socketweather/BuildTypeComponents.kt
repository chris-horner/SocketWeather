package codes.chrishorner.socketweather

import android.app.Activity
import android.app.Application
import android.view.ViewGroup
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.NetworkComponents

/**
 * Generators for things that change depending on the build type being debug or release.
 */
@MainThread
interface BuildTypeComponents {
  fun createRootContainerFor(activity: Activity): ViewGroup
  fun createDeviceLocator(app: Application): DeviceLocator
  fun createNetworkComponents(app: Application): NetworkComponents
}
