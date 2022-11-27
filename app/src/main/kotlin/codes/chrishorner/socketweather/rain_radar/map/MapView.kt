package codes.chrishorner.socketweather.rain_radar.map

import android.preference.PreferenceManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
import org.osmdroid.views.MapView

@Composable
fun rememberMapViewWithLifecycle(): MapView {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    launch(Dispatchers.IO) {
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
    val lifecycleObserver = mapView.lifecycleObserver()
    lifecycle.addObserver(lifecycleObserver)
    onDispose {
      lifecycle.removeObserver(lifecycleObserver)
    }
  }

  return mapView
}

private fun MapView.lifecycleObserver(): LifecycleEventObserver = LifecycleEventObserver { _, event ->
  when (event) {
    ON_RESUME -> onResume()
    ON_PAUSE -> onPause()
    ON_DESTROY -> onDetach()
    else -> { /* osm droid only cares about the above events. */
    }
  }
}
