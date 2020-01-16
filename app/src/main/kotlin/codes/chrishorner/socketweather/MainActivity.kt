package codes.chrishorner.socketweather

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import codes.chrishorner.socketweather.choose_location.ChooseLocationController
import codes.chrishorner.socketweather.data.LocationChoices
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
    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    val rootContainer: ViewGroup = BuildTypeConfig.getRootContainerFor(this)
    router = Conductor.attachRouter(this, rootContainer, savedInstanceState)
    router.addChangeListener(ControllerLeakListener)

    if (!router.hasRootController()) {
      val locationChoices: LocationChoices = getLocationChoices()

      if (locationChoices.getCurrentSelection() == LocationSelection.None) {
        router.setRoot(
            ChooseLocationController(
                displayAsRoot = true,
                showFollowMe = !locationChoices.hasFollowMeSaved()
            ).asTransaction()
        )
      } else {
        router.setRoot(HomeController().asTransaction())
      }
    }
  }

  override fun onStart() {
    super.onStart()
    getDeviceLocator().enable()
  }

  override fun onStop() {
    super.onStop()
    getDeviceLocator().disable()
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
