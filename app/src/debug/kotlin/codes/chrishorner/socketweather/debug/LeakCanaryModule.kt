package codes.chrishorner.socketweather.debug

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.datastore.preferences.core.edit
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.ENABLE_HEAP_DUMPS
import com.alorma.drawer_modules.ActionsModule
import com.alorma.drawer_modules.actions.ButtonAction
import com.alorma.drawer_modules.actions.SwitchAction
import kotlinx.coroutines.launch
import leakcanary.LeakCanary

@Composable
fun LeakCanaryModule() {

  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val preferenceStore = context.debugPreferences
  val heapDumpsEnabled = preferenceStore.blockingGetValue(ENABLE_HEAP_DUMPS) ?: true

  ActionsModule(
    title = "Leaks",
    icon = { Icon(painterResource(R.drawable.ic_water_24dp), contentDescription = null) }
  ) {

    SwitchAction(text = "Enable heap dumps", isChecked = heapDumpsEnabled) { checked ->
      scope.launch {
        preferenceStore.edit { preferences ->
          preferences[ENABLE_HEAP_DUMPS] = checked
        }
      }
    }

    ButtonAction(text = "View leaks") {
      context.startActivity(LeakCanary.newLeakDisplayActivityIntent())
    }
  }
}
