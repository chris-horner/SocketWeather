package codes.chrishorner.socketweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Render under the status and navigation bars.
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      RootContainer {
        Navigation()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    appSingletons.forecastLoader.refreshIfNecessary()
  }
}
