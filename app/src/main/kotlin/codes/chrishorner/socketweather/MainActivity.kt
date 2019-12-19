package codes.chrishorner.socketweather

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import codes.chrishorner.socketweather.util.inflate

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Render under the status and navigation bars.
    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    val rootContainer = BuildTypeConfig.getRootContainerFor(this)
    rootContainer.inflate<ViewGroup>(R.layout.activity_main, attach = true)
  }

  override fun onBackPressed() {
    // Guard against a leak introduced in Android 10.
    // https://twitter.com/Piwai/status/1169274622614704129
    if (isTaskRoot) finishAfterTransition() else super.onBackPressed()
  }
}
