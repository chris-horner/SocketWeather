package codes.chrishorner.socketweather.data

import android.app.Application
import codes.chrishorner.socketweather.data.DebugDeviceLocator.Mode.MOCK
import codes.chrishorner.socketweather.data.DebugDeviceLocator.Mode.REAL
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.DEVICE_LOCATION
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.DEVICE_LOCATION_MODE
import codes.chrishorner.socketweather.debug.blockingGet
import codes.chrishorner.socketweather.debug.blockingGetValue
import codes.chrishorner.socketweather.debug.debugPreferences
import codes.chrishorner.socketweather.debug.getEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DebugDeviceLocator(app: Application) : DeviceLocator {

  enum class Mode { REAL, MOCK }

  private val preferenceStore = app.debugPreferences
  private val androidDeviceLocator = AndroidDeviceLocator(app)

  private val modes: StateFlow<Mode>
  private val mockLocationChoices: StateFlow<String>

  init {
    val mode = preferenceStore.blockingGet().getEnum(DEVICE_LOCATION_MODE) ?: REAL
    val locationName = preferenceStore.blockingGetValue(DEVICE_LOCATION) ?: mockLocations.entries.first().key
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    modes = preferenceStore.data
      .map { preferences -> preferences.getEnum(DEVICE_LOCATION_MODE) ?: REAL }
      .stateIn(scope, SharingStarted.Eagerly, mode)

    mockLocationChoices = preferenceStore.data
      .map { preferences -> preferences[DEVICE_LOCATION] ?: mockLocations.entries.first().key }
      .stateIn(scope, SharingStarted.Eagerly, locationName)
  }

  override suspend fun getLocation(): DeviceLocation? {
    return when (modes.value) {
      REAL -> androidDeviceLocator.getLocation()
      MOCK -> mockLocations[mockLocationChoices.value]
    }
  }

  companion object {
    val mockLocations = mapOf(
      "Melbourne" to DeviceLocation(-37.817691, 144.967311),
      "Canberra" to DeviceLocation(-35.306129, 149.126262),
      "Sydney" to DeviceLocation(-33.857808, 151.214608),
      "Brisbane" to DeviceLocation(-27.460755, 153.032066),
      "Darwin" to DeviceLocation(-12.463112, 130.841628),
      "Perth" to DeviceLocation(-31.956895, 115.859991),
      "Adelaide" to DeviceLocation(-34.928395, 138.599915),
      "Hobart" to DeviceLocation(-42.883274, 147.330152),
      "Tokyo" to DeviceLocation(35.680349, 139.769060)
    )
  }
}
