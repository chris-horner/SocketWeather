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
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.LifecycleEventObserver
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.TilesOverlay

@Composable
fun rememberMapViewWithLifecycle(): MapView {
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
        zoomController.setVisibility(NEVER)
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
  when (event) {
    ON_RESUME -> mapView.onResume()
    ON_PAUSE -> mapView.onPause()
    ON_DESTROY -> mapView.onDetach()
    else -> { /* osm droid only cares about the above events. */ }
  }
}


fun getTileProvider(context: Context): MapTileProviderBase {
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
    "© OpenStreetMap contributors",
    TileSourcePolicy(4, 0)
  ) {
    override fun getTileURLString(pMapTileIndex: Long): String {
      return "$baseUrl/${MapTileIndex.getZoom(pMapTileIndex)}/${MapTileIndex.getX(pMapTileIndex)}/${
        MapTileIndex.getY(
          pMapTileIndex
        )
      }$mImageFilenameEnding"
    }
  }

  return MapTileProviderBasic(context, source)
}

fun getRainRadarOverlays(context: Context, timestamps: List<String>): List<TilesOverlay> {
  return timestamps.map { getRainOverlay(context, it) }
}

val TilesOverlay.isLoading: Boolean
  get() = !tileStates.isDone || tileStates.notFound > 0

private fun getRainOverlay(context: Context, timestamp: String): TilesOverlay {
  val source = object : OnlineTileSourceBase(
    timestamp,
    3,
    10,
    256,
    ".png",
    arrayOf("https://api.weather.bom.gov.au/v1/rainradar/tiles/$timestamp"),
    "© Australian Bureau of Meteorology",
    RadarTileSourcePolicy
  ) {
    override fun getTileURLString(pMapTileIndex: Long): String {
      return "$baseUrl/${MapTileIndex.getZoom(pMapTileIndex)}/${MapTileIndex.getX(pMapTileIndex)}/${
        MapTileIndex.getY(
          pMapTileIndex
        )
      }$mImageFilenameEnding"
    }
  }
  val provider = MapTileProviderBasic(context, source)
  val overlay = TilesOverlay(provider, context)
  overlay.loadingBackgroundColor = Color.TRANSPARENT
  return overlay
}
