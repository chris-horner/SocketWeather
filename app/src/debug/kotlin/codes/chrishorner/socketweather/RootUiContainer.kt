package codes.chrishorner.socketweather

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import codes.chrishorner.socketweather.debug.ComposeDebugDrawer
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun RootContainer(content: @Composable () -> Unit) {
  SocketWeatherTheme {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    SideEffect {
      systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }
    ComposeDebugDrawer {
      Box(modifier = Modifier.fillMaxSize()) {
        content()
      }
    }
  }
}
