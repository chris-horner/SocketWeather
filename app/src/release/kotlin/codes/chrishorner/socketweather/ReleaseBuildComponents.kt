package codes.chrishorner.socketweather

import android.app.Application
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DataConfig
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.RealDeviceLocator
import codes.chrishorner.socketweather.data.ReleaseNetworkComponents

object CurrentBuildTypeComponents : BuildTypeComponents by ReleaseBuildComponents

private object ReleaseBuildComponents : BuildTypeComponents {

  @MainThread
  override fun createDeviceLocator(app: Application): DeviceLocator {
    return RealDeviceLocator(app)
  }

  @MainThread
  override fun createNetworkComponents(app: Application): NetworkComponents {
    return ReleaseNetworkComponents(DataConfig.API_ENDPOINT, DataConfig.moshi)
  }
}
