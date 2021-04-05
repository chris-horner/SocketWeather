package codes.chrishorner.socketweather.debug

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.HTTP_LOG_LEVEL
import com.alorma.drawer_modules.ActionsModule
import com.alorma.drawer_modules.actions.DropdownSelectorAction
import kotlinx.coroutines.launch
import okhttp3.logging.HttpLoggingInterceptor.Level

@Composable
fun LogsModule() {

  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val preferenceStore = context.debugPreferences
  val level = preferenceStore.blockingGet().getEnum(HTTP_LOG_LEVEL) ?: Level.BASIC

  ActionsModule(
    title = "Logs",
    icon = { Icon(Icons.Rounded.Article, contentDescription = null) },
  ) {
    DropdownSelectorAction(
      label = "HTTP level",
      items = Level.values().toList(),
      defaultValue = level
    ) { selectedLevel ->
      scope.launch {
        preferenceStore.edit { preferences ->
          preferences[HTTP_LOG_LEVEL] = selectedLevel.ordinal
        }
      }
    }
  }
}
