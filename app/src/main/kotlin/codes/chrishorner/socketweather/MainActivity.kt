package codes.chrishorner.socketweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import codes.chrishorner.socketweather.choose_location.ChooseLocationScreen
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeScreen
import codes.chrishorner.socketweather.util.navigation.VoyagerNavigation

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Render under the status and navigation bars.
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      RootContainer {
        val initialScreen = remember { calculateInitialScreen() }
        VoyagerNavigation(initialScreen)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    appSingletons.forecastLoader.refreshIfNecessary()
  }

  private fun calculateInitialScreen(): Screen<*, *> {
    val openAtLocationPicker = intent.extras?.getBoolean(EXTRA_OPEN_AT_LOCATION_PICKER) ?: false
    val currentLocationSelection = appSingletons.stores.currentSelection.data.value
    return if (openAtLocationPicker || currentLocationSelection == LocationSelection.None) {
      ChooseLocationScreen(showCloseButton = false)
    } else {
      HomeScreen
    }
  }

  companion object {
    const val EXTRA_OPEN_AT_LOCATION_PICKER = "open_at_location_picker"
  }
}
