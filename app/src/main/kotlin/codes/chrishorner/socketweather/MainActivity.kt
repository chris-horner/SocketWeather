package codes.chrishorner.socketweather

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import codes.chrishorner.socketweather.data.Forecaster
import java.time.Duration
import java.time.Instant

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Render under the status and navigation bars.
    WindowCompat.setDecorFitsSystemWindows(window, false)
    val locationSelection = appSingletons.locationSelectionStore.currentSelection.value

    setContent {
      RootContainer {
        NavGraph(currentSelection = locationSelection)
      }
    }
  }

  override fun onResume() {
    super.onResume()

    val forecaster = appSingletons.forecaster
    val state = forecaster.states.value

    if (state is Forecaster.LoadingState.Idle) {
      forecaster.refresh()
    } else if (state is Forecaster.LoadingState.Loaded) {
      val elapsedTime = Duration.between(state.forecast.updateTime, Instant.now())
      if (elapsedTime.toMinutes() > 1) {
        forecaster.refresh()
      }
    }
  }

  override fun onStart() {
    super.onStart()
    appSingletons.deviceLocator.enable()
  }

  override fun onStop() {
    super.onStop()
    appSingletons.deviceLocator.disable()
  }
}
