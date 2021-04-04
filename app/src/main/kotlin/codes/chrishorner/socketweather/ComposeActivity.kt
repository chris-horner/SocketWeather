package codes.chrishorner.socketweather

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import dev.chrisbanes.accompanist.insets.ExperimentalAnimatedInsets

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
}
