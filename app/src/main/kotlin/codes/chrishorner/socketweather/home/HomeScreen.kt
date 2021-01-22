package codes.chrishorner.socketweather.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import codes.chrishorner.socketweather.Screen
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.home.HomeEvent.ChooseLocation
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.util.InsetAwareTopAppBar
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

enum class HomeEvent { ChooseLocation, Refresh, ViewAbout }

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {
  val state: Forecaster.State by viewModel.states.collectAsState()
  HomeUi(state) { event ->
    when (event) {
      ChooseLocation -> navController.navigate(Screen.ChooseLocation.getRoute())
    }
  }
}

@Composable
private fun HomeUi(state: Forecaster.State, eventHandler: (event: HomeEvent) -> Unit) {
  Surface(color = MaterialTheme.colors.background) {
    Scaffold(
        topBar = {
          InsetAwareTopAppBar(title = {
            // TODO: Add Current location / update time / picker dropdown.
          })
        }
    ) {
      Column {
        Text(text = "Home screen.")
        Button(onClick = { eventHandler(ChooseLocation) }) {
          Text("Choose Location")
        }
      }
    }
  }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_5)
@Composable
private fun HomePreview() {
  SocketWeatherTheme {
    ProvideWindowInsets {
      HomeUi(Forecaster.State.Idle) { /* Don't handle events in preview. */ }
    }
  }
}
