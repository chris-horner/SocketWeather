package codes.chrishorner.socketweather

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import codes.chrishorner.socketweather.data.Forecaster
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import org.threeten.bp.Duration
import org.threeten.bp.Instant

@ExperimentalAnimatedInsets
class ComposeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Render under the status and navigation bars.
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      RootContainer {
        val locationSelection = appSingletons.locationSelectionStore.currentSelection.value
        NavGraph(currentSelection = locationSelection)
      }
    }
  }

  override fun onResume() {
    super.onResume()

    val forecaster = appSingletons.forecaster

    when (val state = forecaster.states.value) {
      is Forecaster.State.Idle -> forecaster.refresh()

      is Forecaster.State.Loaded -> {
        val elapsedTime = Duration.between(state.forecast.updateTime, Instant.now())
        if (elapsedTime.toMinutes() > 1) {
          forecaster.refresh()
        }
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
