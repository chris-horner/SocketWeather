package codes.chrishorner.socketweather.debug

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alorma.drawer_base.DebugDrawerLayout
import com.alorma.drawer_modules.DeviceModule
import com.google.accompanist.insets.systemBarsPadding

@Composable
fun ComposeDebugDrawer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  DebugDrawerLayout(
    enableShake = false,
    modifier = modifier.systemBarsPadding(),
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
