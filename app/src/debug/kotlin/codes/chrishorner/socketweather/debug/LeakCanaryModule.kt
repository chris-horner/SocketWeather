package codes.chrishorner.socketweather.debug

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.datastore.preferences.core.edit
import codes.chrishorner.socketweather.R
import com.alorma.drawer_modules.ActionsModule
import com.alorma.drawer_modules.actions.ButtonAction
import com.alorma.drawer_modules.actions.SwitchAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import leakcanary.LeakCanary

@Composable
fun LeakCanaryModule() {

  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val preferences = context.applicationContext.debugPreferences
  val heapDumpsEnabled: Boolean = runBlocking {  preferences.data
    .map { it[DebugPreferenceKeys.ENABLE_HEAP_DUMPS] ?: true }
    .first()
  }

  ActionsModule(
    title = "Leaks",
    icon = { Icon(painterResource(R.drawable.ic_water_24dp), contentDescription = null) }
  ) {

    SwitchAction(text = "Enable heap dumps", isChecked = heapDumpsEnabled) { checked ->
      scope.launch {
        preferences.edit { it[DebugPreferenceKeys.ENABLE_HEAP_DUMPS] = checked }
      }
    }

    ButtonAction(text = "View leaks") {
      context.startActivity(LeakCanary.newLeakDisplayActivityIntent())
    }
  }
}
