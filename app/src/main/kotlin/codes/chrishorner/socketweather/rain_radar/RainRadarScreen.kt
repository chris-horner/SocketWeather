package codes.chrishorner.socketweather.rain_radar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.styles.LightColors
import codes.chrishorner.socketweather.util.InsetAwareTopAppBar
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.TilesOverlay

@Composable
fun RainRadarScreen(viewModel: RainRadarViewModel, navController: NavController) {
  val state: RainRadarState by viewModel.states.collectAsState()
  val systemUiController = rememberSystemUiController()
  val useDarkIcons = MaterialTheme.colors.isLight

  // Force dark system icons while viewing this screen.
  DisposableEffect(Unit) {
    systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = true)
    onDispose {
      systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }
  }

  MaterialTheme(
    colors = LightColors,
    typography = MaterialTheme.typography,
  ) {
    RainRadarUi(state) { navController.popBackStack() }
  }
}

@Composable
private fun RainRadarUi(state: RainRadarState, onBackPressed: () -> Unit = {}) {
  var loading by remember { mutableStateOf(true) }

  Box {
    RainRadar(state) { loading = it }

    InsetAwareTopAppBar(
      title = { ToolbarTitle(state.subtitle, loading) },
      navigationIcon = {
        IconButton(onClick = onBackPressed) {
          Icon(Rounded.ArrowBack, contentDescription = stringResource(R.string.rainRadar_backDesc))
        }
      },
      backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.6f),
      elevation = 0.dp,
    )
  }
}

@Composable
@Suppress("UNCHECKED_CAST")
private fun RainRadar(state: RainRadarState, setLoading: (Boolean) -> Unit) {
  val context = LocalContext.current
  val rawMapView = rememberMapViewWithLifecycle()

  // Every time timestamps change, remove and reset the MapView's overlays.
  LaunchedEffect(state.timestamps) {
    rawMapView.overlayManager.clear()
    rawMapView.overlays.addAll(getRainRadarOverlays(context, state.timestamps))
  }

  AndroidView(
    {
      rawMapView.apply {
        tileProvider = getTileProvider(context)
        controller.setCenter(GeoPoint(state.location.latitude, state.location.longitude))
        controller.setZoom(state.location.zoom)
      }
    }
  ) { mapView ->
    val overlays = mapView.overlays as List<TilesOverlay>
    setLoading(overlays.any { !it.tileStates.isDone } || overlays.any { it.tileStates.notFound > 0 })

    if (overlays.isNotEmpty()) {
      val previousIndex = (if (state.activeOverlayIndex == 0) overlays.size else state.activeOverlayIndex) - 1
      overlays[previousIndex].isEnabled = false
      overlays[state.activeOverlayIndex].isEnabled = true
      mapView.invalidate()
    }
  }
}

@Composable
private fun ToolbarTitle(subtitle: String, loading: Boolean) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {

    Column(
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .fillMaxHeight()
        .weight(1f)
    ) {
      Text(stringResource(R.string.rainRadar_title), style = MaterialTheme.typography.h5)
      Text(text = subtitle, style = MaterialTheme.typography.caption)
    }

    AnimatedVisibility(
      visible = loading,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      CircularProgressIndicator(
        modifier = Modifier
          .padding(horizontal = 8.dp)
          .size(24.dp)
      )
    }
  }
}
