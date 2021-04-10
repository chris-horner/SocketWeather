package codes.chrishorner.socketweather.debug

import androidx.compose.runtime.Composable
import com.alorma.drawer_base.DebugDrawerLayout
import com.alorma.drawer_modules.DeviceModule

@Composable
fun ComposeDebugDrawer(content: @Composable () -> Unit) {
  DebugDrawerLayout(
    enableShake = false,
    drawerModules = {
      NetworkModule()
      DebugDeviceLocationModule()
      LeakCanaryModule()
      LogsModule()
      DeviceModule()
    }) {
    content()
  }
}
