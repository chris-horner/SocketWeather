package codes.chrishorner.socketweather.debug

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alorma.drawer_base.DebugDrawerLayout
import com.alorma.drawer_modules.DeviceModule

@Composable
fun ComposeDebugDrawer(content: @Composable () -> Unit) {
  DebugDrawerLayout(
    enableShake = false,
    drawerContentModifier = Modifier.systemBarsPadding(),
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
