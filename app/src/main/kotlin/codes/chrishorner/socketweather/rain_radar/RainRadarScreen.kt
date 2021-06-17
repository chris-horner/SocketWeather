package codes.chrishorner.socketweather.rain_radar

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
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.views.CustomZoomButtonsController.Visibility
import org.osmdroid.views.MapView

@Composable
fun RainRadarScreen() {
  val mapView = rememberMapViewWithLifecycle()
  AndroidView({ mapView }) { mapView ->
    // TODO: Make mapView display stuff.
  }
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  LaunchedEffect(Unit) {
    scope.launch(Dispatchers.IO) {
      Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
    }
  }

  val mapView = remember {
    // MapView performs disk operations on the main thread when initialised 😢.
    allowMainThreadDiskOperations {
      MapView(context).apply {
        setMultiTouchControls(true)
        zoomController.setVisibility(Visibility.NEVER)
      }
    }
  }

  val lifecycleObserver = rememberMapLifecycleObserver(mapView)
  val lifecycle = LocalLifecycleOwner.current.lifecycle

  DisposableEffect(lifecycle) {
    lifecycle.addObserver(lifecycleObserver)
    onDispose {
      lifecycle.removeObserver(lifecycleObserver)
    }
  }

  return mapView
}

@Composable
private fun rememberMapLifecycleObserver(mapView: MapView): LifecycleEventObserver = remember(mapView) {
  LifecycleEventObserver { _, event ->
    @Suppress("NON_EXHAUSTIVE_WHEN") // osmdroid only cares about these events.
    when (event) {
      ON_RESUME -> mapView.onResume()
      ON_PAUSE -> mapView.onPause()
      ON_DESTROY -> mapView.onDetach()
    }
  }
}
