package codes.chrishorner.socketweather.rain_radar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.insets.systemBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

@Composable
fun RainRadarScreen() {
  val systemUiController = rememberSystemUiController()
  val useDarkIcons = MaterialTheme.colors.isLight

  // Force dark system icons while viewing Rain Radar Screen.
  DisposableEffect(Unit) {
    systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = true)
    onDispose {
      systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }
  }

  var loading by remember { mutableStateOf(true) }

  Box {
    RainRadar { loading = it }
    AnimatedVisibility(
      visible = loading,
      Modifier.align(Alignment.TopEnd),
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      CircularProgressIndicator(
        modifier = Modifier
          .systemBarsPadding()
          .padding(16.dp)
      )
    }
  }
}

@Composable
private fun RainRadar(setLoading: (Boolean) -> Unit) {
  val rawMapView = rememberMapViewWithLifecycle()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val overlays = remember { getRainRadarOverlays(context) }

  AndroidView({ rawMapView }) { mapView ->

    mapView.tileProvider = getTileProvider(context)
    mapView.controller.setZoom(9.0)
    mapView.controller.setCenter(GeoPoint(-37.80517674019138, 144.98394260916697))
    mapView.overlays.addAll(overlays)

    scope.launch {
      var index = 0

      // While we're on screen, loop through and display each rainfall overlay.
      while (true) {
        val overlay = overlays[index]
        overlay.isEnabled = true
        mapView.invalidate()
        setLoading(overlays.any { it.tileStates.notFound > 0 })

        if (overlay.tileStates.notFound == 0) {
          // Pause on each overlay for 500ms, or 1s if it's the last.
          if (index == overlays.size - 1) delay(1_000) else delay(500)
        } else {
          while (overlay.tileStates.notFound > 0) {
            delay(500)
          }
        }

        overlay.isEnabled = false
        index++
        if (index >= overlays.size) index = 0
      }
    }
  }
}
