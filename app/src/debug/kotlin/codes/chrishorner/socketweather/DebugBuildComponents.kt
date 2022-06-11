package codes.chrishorner.socketweather

import android.app.Application
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DataConfig
import codes.chrishorner.socketweather.data.DebugDeviceLocator
import codes.chrishorner.socketweather.data.DebugNetworkComponents
import codes.chrishorner.socketweather.data.DeviceLocation
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.Store

object CurrentBuildTypeComponents : BuildTypeComponents by DebugBuildComponents

@MainThread
private object DebugBuildComponents : BuildTypeComponents {

  private var deviceLocator: DebugDeviceLocator? = null

  override fun createDeviceLocator(app: Application, lastKnownLocation: Store<DeviceLocation?>): DeviceLocator {
    deviceLocator?.let { return it }
    return DebugDeviceLocator(app, lastKnownLocation).also { deviceLocator = it }
  }

  private var networkComponents: DebugNetworkComponents? = null

  override fun createNetworkComponents(app: Application): NetworkComponents {
    networkComponents?.let { return it }
    return DebugNetworkComponents(app, DataConfig.API_ENDPOINT, DataConfig.moshi).also { networkComponents = it }
  }
}
