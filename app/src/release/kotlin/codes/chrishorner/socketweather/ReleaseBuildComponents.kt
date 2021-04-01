package codes.chrishorner.socketweather

import android.app.Activity
import android.app.Application
import android.view.ViewGroup
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.RealDeviceLocator
import codes.chrishorner.socketweather.data.ReleaseNetworkComponents
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout

object CurrentBuildTypeComponents : BuildTypeComponents by ReleaseBuildComponents

private object ReleaseBuildComponents : BuildTypeComponents {

  @MainThread
  override fun createRootContainerFor(activity: Activity): ViewGroup {
    val content = activity.findViewById(android.R.id.content) as ViewGroup
    val container = ChangeHandlerFrameLayout(activity)
    content.addView(container)
    return container
  }

  @MainThread
  override fun createDeviceLocator(app: Application): DeviceLocator {
    return RealDeviceLocator(app)
  }

  @MainThread
  override fun createNetworkComponents(app: Application): NetworkComponents {
    return ReleaseNetworkComponents()
  }
}
