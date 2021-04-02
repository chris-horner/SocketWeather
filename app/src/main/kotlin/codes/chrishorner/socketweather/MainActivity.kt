package codes.chrishorner.socketweather

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import codes.chrishorner.socketweather.choose_location.ChooseLocationController
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeController
import codes.chrishorner.socketweather.util.ControllerLeakListener
import codes.chrishorner.socketweather.util.asTransaction
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router

class MainActivity : AppCompatActivity() {

  private lateinit var router: Router

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Render under the status and navigation bars.
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val rootContainer: ViewGroup = CurrentBuildTypeComponents.createRootContainerFor(this)
    router = Conductor.attachRouter(this, rootContainer, savedInstanceState)
    router.addChangeListener(ControllerLeakListener)

    if (!router.hasRootController()) {
      val locationSelection = appSingletons.locationSelectionStore.currentSelection.value

      if (locationSelection == LocationSelection.None) {
        router.setRoot(ChooseLocationController(displayAsRoot = true).asTransaction())
      } else {
        router.setRoot(HomeController().asTransaction())
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

  override fun onBackPressed() {
    if (!router.handleBack()) {
      // Guard against a leak introduced in Android 10.
      // https://twitter.com/Piwai/status/1169274622614704129
      finishAfterTransition()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    router.removeChangeListener(ControllerLeakListener)
  }
}
