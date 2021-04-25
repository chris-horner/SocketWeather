package codes.chrishorner.socketweather.util

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
private fun <I, O> ActivityResultRegistry.activityResultLauncher(
  requestContract: ActivityResultContract<I, O>,
  onResult: (O) -> Unit
): ActivityResultLauncher<I> {
  val key = currentCompositeKeyHash.toString()
  val launcher = remember(requestContract, onResult) {
    register(key, requestContract, onResult)
  }

  DisposableEffect(launcher) {
    onDispose { launcher.unregister() }
  }

  return launcher
}

class PermissionState(
  private val permission: String,
  hasPermissionState: State<Boolean>,
  private val launcher: ActivityResultLauncher<String>
) {
  val hasPermission by hasPermissionState

  fun launchPermissionRequest() = launcher.launch(permission)
}

@Composable
fun ActivityResultRegistry.permissionState(
  permission: String,
  onGrantedChange: ((granted: Boolean) -> Unit)? = null,
): PermissionState {
  val context = LocalContext.current
  val permissionState = remember {
    mutableStateOf(context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED)
  }
  val launcher = activityResultLauncher(ActivityResultContracts.RequestPermission()) {
    permissionState.value = it
    onGrantedChange?.invoke(it)
  }
  return remember(launcher) {
    PermissionState(permission, permissionState, launcher)
  }
}
