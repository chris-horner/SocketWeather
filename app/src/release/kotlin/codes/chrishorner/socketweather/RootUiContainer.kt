package codes.chrishorner.socketweather

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RootContainer(content: @Composable () -> Unit) {
  val settingsStore = LocalContext.current.appSingletons.stores.settings
  val settings by remember(settingsStore) { settingsStore.data }.collectAsState()

  SocketWeatherTheme(dynamicColor = settings.useDynamicColors) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    SideEffect {
      systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }
    Box(
      modifier = Modifier
        .fillMaxSize()
        .semantics {
          testTagsAsResourceId = BuildConfig.BUILD_TYPE == "benchmark"
        }
    ) {
      content()
    }
  }
}
