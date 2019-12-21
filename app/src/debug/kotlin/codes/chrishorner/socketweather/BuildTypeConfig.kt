package codes.chrishorner.socketweather

import android.app.Activity
import android.os.StrictMode
import android.view.ViewGroup
import au.com.gridstone.debugdrawer.DebugDrawer
import au.com.gridstone.debugdrawer.DeviceInfoModule

object BuildTypeConfig {

  fun getRootContainerFor(activity: Activity): ViewGroup {
    // Temporarily allow disk reads on main thread to allow debug drawer
    // modules to do their thing.
    val diskReadPolicy = StrictMode.allowThreadDiskReads()
    val container = DebugDrawer.with(activity)
        .addSectionTitle("Device information")
        .addModule(DeviceInfoModule())
        .buildMainContainer()

    StrictMode.setThreadPolicy(diskReadPolicy)
    return container
  }
}
