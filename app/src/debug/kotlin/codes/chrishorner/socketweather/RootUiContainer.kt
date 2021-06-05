package codes.chrishorner.socketweather

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import codes.chrishorner.socketweather.debug.ComposeDebugDrawer
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
@OptIn(ExperimentalAnimatedInsets::class)
fun RootContainer(content: @Composable () -> Unit) {
  SocketWeatherTheme {
    ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
      val systemUiController = rememberSystemUiController()
      val useDarkIcons = MaterialTheme.colors.isLight
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
}
