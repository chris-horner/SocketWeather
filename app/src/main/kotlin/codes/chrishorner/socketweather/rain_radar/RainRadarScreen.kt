package codes.chrishorner.socketweather.rain_radar

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons.Rounded
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import codes.chrishorner.socketweather.Navigator
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.Screen
import codes.chrishorner.socketweather.rain_radar.map.getRainRadarOverlays
import codes.chrishorner.socketweather.rain_radar.map.getTileProvider
import codes.chrishorner.socketweather.rain_radar.map.isLoading
import codes.chrishorner.socketweather.rain_radar.map.rememberMapViewWithLifecycle
import codes.chrishorner.socketweather.styles.LightColorScheme
import codes.chrishorner.socketweather.styles.copyright
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.parcelize.Parcelize
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.TilesOverlay

@Parcelize
object RainRadarScreen : Screen<RainRadarBackPressEvent, RainRadarState> {

  override fun onCreatePresenter(
    context: Context,
    navigator: Navigator
  ) = RainRadarPresenter(context, navigator)

  @Composable
  override fun Content(state: RainRadarState, onEvent: (RainRadarBackPressEvent) -> Unit) {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    // Force dark system icons while viewing this screen.
    DisposableEffect(Unit) {
      systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = true)
      onDispose {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
      }
    }

    // Force a light theme while viewing this screen.
    MaterialTheme(
      colorScheme = LightColorScheme,
      typography = MaterialTheme.typography,
    ) {
      RainRadarUi(state) { onEvent(RainRadarBackPressEvent) }
    }
  }
}

@Composable
private fun RainRadarUi(state: RainRadarState, onBackPressed: () -> Unit = {}) {
  var loading by remember { mutableStateOf(true) }
  val context = LocalContext.current

  Box {
    RainRadar(state) { loading = it }

    TopAppBar(
      title = { ToolbarTitle(state.subtitle, loading) },
      navigationIcon = {
        IconButton(onClick = onBackPressed) {
          Icon(Rounded.ArrowBack, contentDescription = stringResource(R.string.rainRadar_backDesc))
        }
      },
      colors = TopAppBarDefaults.smallTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
      ),
    )

    ClickableText(
      text = CopyrightText,
      style = MaterialTheme.typography.copyright,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .systemBarsPadding()
        .padding(8.dp)
    ) { offset ->
      CopyrightText.getStringAnnotations(tag = TAG_URL, start = offset, end = offset)
        .firstOrNull()
        ?.let { annotation ->
          context.startActivity(Intent(ACTION_VIEW, annotation.item.toUri()))
        }
    }
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
    setLoading(overlays.any { it.isLoading })

    for (overlay in overlays) {
      val loading = overlay.isLoading
      // Set each overlay to enabled only if it's loading. Otherwise set its alpha to zero.
      // This enables the overlay to continue downloading tiles but hide if it's not for the
      // current `activeTimestampIndex`.
      overlay.isEnabled = loading
      overlay.setColorFilter(if (loading) ZeroAlphaFilter else null)
    }

    if (overlays.isNotEmpty()) {
      overlays[state.activeTimestampIndex].isEnabled = true
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
      Text(stringResource(R.string.rainRadar_title), style = MaterialTheme.typography.headlineSmall)
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
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

private val ZeroAlphaFilter = PorterDuffColorFilter(0, PorterDuff.Mode.CLEAR)
private const val TAG_URL = "url"
private val CopyrightText = buildAnnotatedString {
  append("Map tiles by ")

  pushStringAnnotation(TAG_URL, annotation = "https://stamen.com")
  withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
    append("Stamen Design")
  }
  pop()

  append(". Data by ")

  pushStringAnnotation(TAG_URL, annotation = "https://openstreetmap.org")
  withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
    append("OpenStreetMap")
  }
  pop()

  append(".")
}
