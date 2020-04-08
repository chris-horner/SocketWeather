package codes.chrishorner.socketweather

import android.app.Activity
import android.view.ViewGroup
import au.com.gridstone.debugdrawer.DebugDrawer
import au.com.gridstone.debugdrawer.DeviceInfoModule
import au.com.gridstone.debugdrawer.leakcanary.LeakCanaryModule
import au.com.gridstone.debugdrawer.okhttplogs.OkHttpLoggerModule
import au.com.gridstone.debugdrawer.retrofit.RetrofitModule
import au.com.gridstone.debugdrawer.timber.TimberModule
import codes.chrishorner.socketweather.data.DebugDeviceLocator
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.debug.DebugDeviceLocatorModule
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations
import com.bluelinelabs.conductor.ChangeHandlerFrameLayout

object BuildTypeConfig {

  fun getRootContainerFor(activity: Activity): ViewGroup {
    val drawerBuilder: DebugDrawer.Builder = DebugDrawer.with(activity)
    // Temporarily allow disk operations on main thread to allow debug drawer
    // modules to do their thing.
    allowMainThreadDiskOperations {
      val networkComponents: NetworkComponents = activity.getNetworkComponents()
      val deviceLocator: DebugDeviceLocator = activity.getDebugDeviceLocator()
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
}
