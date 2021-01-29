package codes.chrishorner.socketweather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.util.InsetAwareTopAppBar
import dev.chrisbanes.accompanist.insets.ExperimentalAnimatedInsets
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

@ExperimentalAnimatedInsets
class ComposeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Render under the status and navigation bars.
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
        SocketWeatherTheme {
          Box(modifier = Modifier.fillMaxSize()) {
            NavGraph(currentSelection = appSingletons.locationChoices.currentSelection)
          }
        }
      }
    }
  }
}

@Composable
private fun TestLayout() {
  Scaffold(
      topBar = {
        InsetAwareTopAppBar(
            title = { Text("Test") },
            backgroundColor = MaterialTheme.colors.background,
            elevation = 0.dp,
            actions = { Menu() }
        )
      }
  ) {
    Text(text = "Hello")
  }
}

@Composable
private fun Menu() {
  var expanded by remember { mutableStateOf(false) }
  val iconButton = @Composable {
    IconButton(onClick = { expanded = true }) {
      Icon(Icons.Default.MoreVert, contentDescription = null)
    }
  }

  DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      toggle = iconButton,
      dropdownOffset = DpOffset(0.dp, (-56).dp)
  ) {
    DropdownMenuItem(onClick = { /* Handle refresh! */ }) {
      Text("Refresh")
    }
    DropdownMenuItem(onClick = { /* Handle settings! */ }) {
      Text("Settings")
    }
    Divider()
    DropdownMenuItem(onClick = { /* Handle send feedback! */ }) {
      Text("Send Feedback")
    }
  }
}
