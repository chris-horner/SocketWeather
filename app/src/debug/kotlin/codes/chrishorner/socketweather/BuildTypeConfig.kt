package codes.chrishorner.socketweather

import android.app.Activity
import android.view.ViewGroup
import au.com.gridstone.debugdrawer.DebugDrawer
import au.com.gridstone.debugdrawer.DeviceInfoModule
import au.com.gridstone.debugdrawer.leakcanary.LeakCanaryModule
import au.com.gridstone.debugdrawer.okhttplogs.OkHttpLoggerModule
import au.com.gridstone.debugdrawer.retrofit.RetrofitModule
import au.com.gridstone.debugdrawer.timber.TimberModule
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.networkComponents
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations

object BuildTypeConfig {

  fun getRootContainerFor(activity: Activity): ViewGroup {
    val drawerBuilder: DebugDrawer.Builder = DebugDrawer.with(activity)
    // Temporarily allow disk operations on main thread to allow debug drawer
    // modules to do their thing.
    allowMainThreadDiskOperations {
      val networkComponents: NetworkComponents = activity.networkComponents()
          drawerBuilder
          .addSectionTitle("Network")
          .addModule(RetrofitModule(networkComponents.debugRetrofitConfig))
          .addSectionTitle("Logs")
          .addModule(OkHttpLoggerModule(networkComponents.httpLogger))
          .addModule(TimberModule())
          .addSectionTitle("Leaks")
          .addModule(LeakCanaryModule)
          .addSectionTitle("Device information")
          .addModule(DeviceInfoModule())
    }

    return drawerBuilder.buildMainContainer()
  }
}
