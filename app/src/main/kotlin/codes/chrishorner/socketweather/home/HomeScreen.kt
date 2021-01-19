package codes.chrishorner.socketweather.home

import android.content.res.Configuration
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.util.InsetAwareTopAppBar
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_5)
@Composable
fun HomePreview() {
  SocketWeatherTheme {
    ProvideWindowInsets {
      HomeUi(Forecaster.State.Idle) {

      }
    }
  }
}

enum class HomeEvent { ChooseLocation, Refresh, ViewAbout }

@Composable
fun HomeScreen(navController: NavHostController, forecaster: Forecaster) {
  val viewModel = remember { HomeViewModel(forecaster) }
}

@Composable
fun HomeUi(state: Forecaster.State, eventHandler: ((event: HomeEvent) -> Unit)? = null) {
  Surface(color = MaterialTheme.colors.background) {
    Scaffold(
        topBar = {
          InsetAwareTopAppBar(title = {
            // TODO: Add Current location / update time / picker dropdown.
          })
        }
    ) {
      Text(text = "Home screen.")
    }
  }
}
