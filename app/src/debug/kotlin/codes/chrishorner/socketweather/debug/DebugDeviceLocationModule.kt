package codes.chrishorner.socketweather.debug

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import codes.chrishorner.socketweather.data.DebugDeviceLocator
import codes.chrishorner.socketweather.data.DebugDeviceLocator.Mode
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.DEVICE_LOCATION
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.DEVICE_LOCATION_MODE
import com.alorma.drawer_modules.ActionsModule
import com.alorma.drawer_modules.actions.DropdownSelectorAction
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun DebugDeviceLocationModule() {

  val scope = rememberCoroutineScope()
  val preferenceStore = LocalContext.current.debugPreferences
  val preferences = preferenceStore.blockingGet()
  val mode = preferences.getEnum(DEVICE_LOCATION_MODE) ?: Mode.REAL

  ActionsModule(
    title = "Device location",
    icon = { Icon(Icons.Rounded.LocationOn, contentDescription = null) },
  ) {

    DropdownSelectorAction(
      label = "Mode",
      items = Mode.values().toList(),
      defaultValue = mode
    ) { selectedMode ->
      scope.launch {
        preferenceStore.edit { preferences ->
          preferences[DEVICE_LOCATION_MODE] = selectedMode.ordinal
        }
      }
    }

    val currentMode by preferenceStore.data
      .map { preferences -> preferences.getEnum(DEVICE_LOCATION_MODE) ?: Mode.REAL }
      .collectAsState(initial = mode)

    AnimatedVisibility(visible = currentMode == Mode.MOCK) {
      DropdownSelectorAction(
        label = "Location",
        items = DebugDeviceLocator.mockLocations.keys.toList(),
        defaultValue = preferences[DEVICE_LOCATION] ?: DebugDeviceLocator.mockLocations.entries.first().key
      ) { selectedLocation ->
        scope.launch {
          preferenceStore.edit { preferences ->
            preferences[DEVICE_LOCATION] = selectedLocation
          }
        }
      }
    }
  }
}
