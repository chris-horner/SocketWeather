package codes.chrishorner.socketweather

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayDeque
import java.util.Deque

class MainActivity : AppCompatActivity() {

  private val backstack: Deque<Screen> = ArrayDeque(3)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Render under the status and navigation bars.
    window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    val rootContainer = BuildTypeConfig.getRootContainerFor(this)

    val backstackEntries = savedInstanceState?.getIntArray(BACKSTACK_KEY)
      ?.map { Screen.values()[it] }
      ?: listOf(Screen.Home)

    backstack.addAll(backstackEntries)

    val initialView = backstackEntries[0].getView(rootContainer)
    rootContainer.addView(initialView)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putIntArray(BACKSTACK_KEY, backstack.map { it.ordinal }.toIntArray())
  }

  override fun onBackPressed() {
    if (backstack.size > 1) {
      backstack.pop()
    } else {
      // Guard against a leak introduced in Android 10.
      // https://twitter.com/Piwai/status/1169274622614704129
      finishAfterTransition()
    }
  }
}

private const val BACKSTACK_KEY = "backstack"
