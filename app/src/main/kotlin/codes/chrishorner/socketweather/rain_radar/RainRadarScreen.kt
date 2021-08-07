package codes.chrishorner.socketweather.rain_radar

import android.content.Context
import android.graphics.Color
import android.preference.PreferenceManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.LifecycleEventObserver
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.generateRainRadarTimestamps
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex.getX
import org.osmdroid.util.MapTileIndex.getY
import org.osmdroid.util.MapTileIndex.getZoom
import org.osmdroid.views.CustomZoomButtonsController.Visibility
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay

@Composable
fun RainRadarScreen() {
  val rawMapView = rememberMapViewWithLifecycle()

  val context = LocalContext.current
  val overlays = remember {
    generateRainRadarTimestamps().map { timestamp -> getRainOverlay(context, timestamp) }
  }
  val scope = rememberCoroutineScope()

  AndroidView({ rawMapView }) { mapView ->

    mapView.tileProvider = getTileProvider(context)
    mapView.controller.setZoom(9.0)
    mapView.controller.setCenter(GeoPoint(-37.80517674019138, 144.98394260916697))

    mapView.overlays.addAll(overlays)

    scope.launch {
      var index = 0

      // While we're on screen, loop through and display each rainfall overlay.
      while (true) {
        overlays[index].isEnabled = true
        mapView.invalidate()

        // Pause on each overlay for 500ms, or 1s if it's the last.
        if (index == overlays.size - 1) delay(1_000) else delay(500)

        overlays[index].isEnabled = false
        index++
        if (index >= overlays.size) index = 0
      }
    }
  }
}

private fun getTileProvider(context: Context): MapTileProviderBase {
  val source = object : OnlineTileSourceBase(
    "Terrain",
    0,
    18,
    256,
    "@2x.png",
    arrayOf(
      "https://stamen-tiles-a.a.ssl.fastly.net/terrain",
      "https://stamen-tiles-b.a.ssl.fastly.net/terrain",
      "https://stamen-tiles-c.a.ssl.fastly.net/terrain",
      "https://stamen-tiles-d.a.ssl.fastly.net/terrain",
    ),
  ) {
    override fun getTileURLString(pMapTileIndex: Long): String {
      return "$baseUrl/${getZoom(pMapTileIndex)}/${getX(pMapTileIndex)}/${getY(pMapTileIndex)}$mImageFilenameEnding"
    }
  }

  return MapTileProviderBasic(context, source)
}

private fun getRainOverlay(context: Context, timestamp: String): TilesOverlay {
  val source = object : OnlineTileSourceBase(
    timestamp,
    3,
    10,
    256,
    ".png",
    arrayOf("https://api.weather.bom.gov.au/v1/rainradar/tiles/$timestamp")
  ) {
    override fun getTileURLString(pMapTileIndex: Long): String {
      return "$baseUrl/${getZoom(pMapTileIndex)}/${getX(pMapTileIndex)}/${getY(pMapTileIndex)}$mImageFilenameEnding"
    }
  }
  val provider = MapTileProviderBasic(context, source)
  val overlay = TilesOverlay(provider, context)
  overlay.loadingBackgroundColor = Color.TRANSPARENT
  overlay.isEnabled = false
  return overlay
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  LaunchedEffect(Unit) {
    scope.launch(Dispatchers.IO) {
      // This API isn't going anywhere, and it's not worth pulling in the AndroidX version.
      @Suppress("DEPRECATION")
      Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
    }
  }

  val mapView = remember {
    // MapView performs disk operations on the main thread when initialised 😢.
    allowMainThreadDiskOperations {
      MapView(context).apply {
        id = R.id.map
        setMultiTouchControls(true)
        zoomController.setVisibility(Visibility.NEVER)
        isTilesScaledToDpi = true
      }
    }
  }

  val lifecycle = LocalLifecycleOwner.current.lifecycle
  DisposableEffect(lifecycle, mapView) {
    val lifecycleObserver = getMapLifecycleObserver(mapView)
    lifecycle.addObserver(lifecycleObserver)
    onDispose {
      lifecycle.removeObserver(lifecycleObserver)
    }
  }

  return mapView
}

private fun getMapLifecycleObserver(
  mapView: MapView
): LifecycleEventObserver = LifecycleEventObserver { _, event ->
  @Suppress("NON_EXHAUSTIVE_WHEN") // osmdroid only cares about these events.
  when (event) {
    ON_RESUME -> mapView.onResume()
    ON_PAUSE -> mapView.onPause()
    ON_DESTROY -> mapView.onDetach()
  }
}
