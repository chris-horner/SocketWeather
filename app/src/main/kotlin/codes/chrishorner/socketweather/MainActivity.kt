package codes.chrishorner.socketweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import codes.chrishorner.socketweather.choose_location.ChooseLocationScreen
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeScreen
import codes.chrishorner.socketweather.util.VoyagerNavigation

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Render under the status and navigation bars.
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      RootContainer {
        val currentLocationSelection = remember { appSingletons.stores.currentSelection.data.value }
        val initialScreen =
          if (currentLocationSelection != LocationSelection.None) HomeScreen
          else ChooseLocationScreen(showCloseButton = false)

        VoyagerNavigation(initialScreen)
      }
    }
  }

  override fun onResume() {
    super.onResume()
    appSingletons.forecastLoader.refreshIfNecessary()
  }
}
