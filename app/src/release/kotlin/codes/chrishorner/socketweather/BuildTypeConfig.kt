package codes.chrishorner.socketweather

import android.app.Activity
import android.app.Application
import android.view.ViewGroup
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.RealDeviceLocator
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout

object BuildTypeConfig {

  @MainThread
  fun getRootContainerFor(activity: Activity): ViewGroup {
    val content = activity.findViewById(android.R.id.content) as ViewGroup
    val container = ChangeHandlerFrameLayout(activity)
    content.addView(container)
    return container
  }

  private var deviceLocator: DeviceLocator? = null

  @MainThread
  fun getDeviceLocator(app: Application): DeviceLocator {
    deviceLocator?.let { return it }
    return RealDeviceLocator(app).also { deviceLocator = it }
  }
}
