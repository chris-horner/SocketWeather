package codes.chrishorner.socketweather.debug

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Article
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.data.DebugNetworkComponents
import com.alorma.drawer_modules.ActionsModule
import com.alorma.drawer_modules.actions.DropdownSelectorAction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor.Level

@Composable
fun LogsModule() {

  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val preferences = context.debugPreferences
  val level: Level = runBlocking {
    preferences.data
      .map { it[DebugPreferenceKeys.HTTP_LOG_LEVEL] ?: Level.BASIC.ordinal }
      .map { index -> Level.values()[index] }
      .first()
  }

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
        preferences.edit { it[DebugPreferenceKeys.HTTP_LOG_LEVEL] = selectedLevel.ordinal }
        val networkComponents = context.appSingletons.networkComponents as DebugNetworkComponents
        networkComponents.httpLogger2.level = selectedLevel
      }
    }
  }
}
