package codes.chrishorner.socketweather.debug

import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.ENDPOINT
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_DELAY
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_ERROR_CODE
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_ERROR_RATE
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_FAIL_RATE
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_VARIANCE
import com.alorma.drawer_modules.ActionsModule
import com.alorma.drawer_modules.actions.DropdownSelectorAction
import kotlinx.coroutines.launch

@Composable
fun NetworkModule() {

  val scope = rememberCoroutineScope()
  val preferenceStore = LocalContext.current.debugPreferences
  val preferences = preferenceStore.blockingGet()
  val endpoint = preferences.getEnum(ENDPOINT) ?: DebugEndpoint.MOCK
  val delay = preferences[MOCK_HTTP_DELAY] ?: 1_000L
  val variance = preferences[MOCK_HTTP_VARIANCE] ?: 40
  val failRate = preferences[MOCK_HTTP_FAIL_RATE] ?: 0
  val errorRate = preferences[MOCK_HTTP_ERROR_RATE] ?: 0
  val errorCode = preferences[MOCK_HTTP_ERROR_CODE] ?: 500

  ActionsModule(
    title = "Network",
    icon = { Icon(Icons.Rounded.Wifi, contentDescription = null) },
  ) {

    DropdownSelectorAction(
      label = "Endpoint",
      items = DebugEndpoint.values().toList(),
      defaultValue = endpoint
    ) { selectedEndpoint ->
      scope.launch {
        preferenceStore.edit { preferences ->
          preferences[ENDPOINT] = selectedEndpoint.ordinal
        }
      }
    }

    if (endpoint != DebugEndpoint.MOCK) return@ActionsModule

    DropdownSelectorAction(
      label = "Delay",
      itemFormatter = { "${it}ms" },
      items = listOf(50L, 500L, 1_000L, 2_000L, 3_000L, 5_000L),
      defaultValue = delay,
    ) { selectedDelay ->
      scope.launch {
        preferenceStore.edit { preferences ->
          preferences[MOCK_HTTP_DELAY] = selectedDelay
        }
      }
    }

    DropdownSelectorAction(
      label = "Variance",
      itemFormatter = { "±$it" },
      items = listOf(20, 40, 60),
      defaultValue = variance,
    ) { selectedVariance ->
      scope.launch {
        preferenceStore.edit { preferences ->
          preferences[MOCK_HTTP_VARIANCE] = selectedVariance
        }
      }
    }

    DropdownSelectorAction(
      label = "Fail rate",
      itemFormatter = { it.formatAsRatePercent() },
      items = listOf(0, 1, 3, 10, 25, 50, 75, 100),
      defaultValue = failRate,
    ) { selectedRate ->
      scope.launch {
        preferenceStore.edit { preferences ->
          preferences[MOCK_HTTP_FAIL_RATE] = selectedRate
        }
      }
    }

    DropdownSelectorAction(
      label = "Error rate",
      itemFormatter = { it.formatAsRatePercent() },
      items = listOf(0, 1, 3, 10, 25, 50, 75, 100),
      defaultValue = errorRate,
    ) { selectedRate ->
      scope.launch {
        preferenceStore.edit { preferences ->
          preferences[MOCK_HTTP_FAIL_RATE] = selectedRate
        }
      }
    }

    DropdownSelectorAction(
      label = "Error code",
      itemFormatter = { "HTTP $it" },
      items = listOf(400, 401, 403, 500, 501, 503, 504),
      defaultValue = errorCode,
    ) { selectedCode ->
      scope.launch {
        preferenceStore.edit { preferences ->
          preferences[MOCK_HTTP_ERROR_CODE] = selectedCode
        }
      }
    }
  }
}

private fun Int.formatAsRatePercent(): String = when (this) {
  0 -> "None"
  100 -> "All"
  else -> "$this%"
}
