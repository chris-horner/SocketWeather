package codes.chrishorner.socketweather.rain_radar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle.Event.ON_ANY
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.LifecycleEventObserver
import codes.chrishorner.socketweather.R
import com.google.android.libraries.maps.MapView

@Composable
fun RainRadarScreen2() {
  val view = rememberMapViewWithLifecycle()
  AndroidView({ view }) { mapView ->
    // TODO: Do stuff with mapView.
  }
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
  val context = LocalContext.current

  val mapView = remember {
    MapView(context).apply { id = R.id.map }
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
    ON_CREATE -> mapView.onCreate(null)
    ON_START -> mapView.onStart()
    ON_RESUME -> mapView.onResume()
    ON_PAUSE -> mapView.onPause()
    ON_STOP -> mapView.onStop()
    ON_DESTROY -> mapView.onDestroy()
    ON_ANY -> Unit
  }
}
