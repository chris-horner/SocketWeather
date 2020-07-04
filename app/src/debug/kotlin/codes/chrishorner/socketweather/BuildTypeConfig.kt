package codes.chrishorner.socketweather

import android.app.Activity
import android.app.Application
import android.view.ViewGroup
import androidx.annotation.MainThread
import au.com.gridstone.debugdrawer.DebugDrawer
import au.com.gridstone.debugdrawer.DeviceInfoModule
import au.com.gridstone.debugdrawer.leakcanary.LeakCanaryModule
import au.com.gridstone.debugdrawer.okhttplogs.OkHttpLoggerModule
import au.com.gridstone.debugdrawer.retrofit.RetrofitModule
import au.com.gridstone.debugdrawer.timber.TimberModule
import codes.chrishorner.socketweather.data.DebugDeviceLocator
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.debug.DebugDeviceLocatorModule
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations
import codes.chrishorner.socketweather.util.app
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout

object BuildTypeConfig {

  @MainThread
  fun getRootContainerFor(activity: Activity): ViewGroup {
    val drawerBuilder: DebugDrawer.Builder = DebugDrawer.with(activity)
    // Temporarily allow disk operations on main thread to allow debug drawer
    // modules to do their thing.
    allowMainThreadDiskOperations {
      val networkComponents: NetworkComponents = activity.appSingletons.networkComponents
      val deviceLocator = getDeviceLocator(activity.app) as DebugDeviceLocator
      drawerBuilder
          .addSectionTitle("Device location")
          .addModule(DebugDeviceLocatorModule(deviceLocator))
          .addSectionTitle("Network")
          .addModule(RetrofitModule(networkComponents.debugRetrofitConfig))
          .addSectionTitle("Logs")
          .addModule(OkHttpLoggerModule(networkComponents.httpLogger))
          .addModule(TimberModule())
          .addSectionTitle("Leaks")
          .addModule(LeakCanaryModule)
          .addSectionTitle("Device information")
          .addModule(DeviceInfoModule())
          .overrideMainContainer(ChangeHandlerFrameLayout(activity))
    }

    return drawerBuilder.buildMainContainer()
  }

  private var deviceLocator: DebugDeviceLocator? = null

  @MainThread
  fun getDeviceLocator(app: Application): DeviceLocator {
    deviceLocator?.let { return it }
    return DebugDeviceLocator(app).also { deviceLocator = it }
  }
}
