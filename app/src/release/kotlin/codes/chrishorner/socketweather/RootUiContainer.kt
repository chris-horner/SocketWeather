package codes.chrishorner.socketweather

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import dev.chrisbanes.accompanist.insets.ExperimentalAnimatedInsets
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@Composable
@ExperimentalAnimatedInsets
fun RootContainer(content: @Composable () -> Unit) {
  ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
    SocketWeatherTheme {
      Box(modifier = Modifier.fillMaxSize()) {
        content()
      }
    }
  }
}
