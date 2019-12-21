package codes.chrishorner.socketweather

import android.app.Activity
import android.os.StrictMode
import android.view.ViewGroup
import au.com.gridstone.debugdrawer.DebugDrawer
import au.com.gridstone.debugdrawer.DeviceInfoModule
import au.com.gridstone.debugdrawer.leakcanary.LeakCanaryModule
import au.com.gridstone.debugdrawer.okhttplogs.OkHttpLoggerModule
import au.com.gridstone.debugdrawer.retrofit.RetrofitModule
import au.com.gridstone.debugdrawer.timber.TimberModule
import codes.chrishorner.socketweather.data.NetworkComponents

object BuildTypeConfig {

  fun getRootContainerFor(activity: Activity): ViewGroup {
    // Temporarily allow disk operations on main thread to allow debug drawer
    // modules to do their thing.
    val diskReadPolicy = StrictMode.allowThreadDiskReads()
    val diskWritePolicy = StrictMode.allowThreadDiskWrites()
    val networkComponents = NetworkComponents.from(activity)
    val container = DebugDrawer.with(activity)
        .addSectionTitle("Network")
        .addModule(RetrofitModule(networkComponents.debugRetrofitConfig))
        .addSectionTitle("Logs")
        .addModule(OkHttpLoggerModule(networkComponents.httpLogger))
        .addModule(TimberModule())
        .addSectionTitle("Leaks")
        .addModule(LeakCanaryModule)
        .addSectionTitle("Device information")
        .addModule(DeviceInfoModule())
        .buildMainContainer()

    StrictMode.setThreadPolicy(diskReadPolicy)
    StrictMode.setThreadPolicy(diskWritePolicy)
    return container
  }
}
