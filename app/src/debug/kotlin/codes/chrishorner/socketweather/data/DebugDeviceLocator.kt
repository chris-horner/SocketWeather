package codes.chrishorner.socketweather.data

import android.app.Application
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.DEVICE_LOCATION
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.DEVICE_LOCATION_MODE
import codes.chrishorner.socketweather.debug.blockingGet
import codes.chrishorner.socketweather.debug.blockingGetValue
import codes.chrishorner.socketweather.debug.debugPreferences
import codes.chrishorner.socketweather.debug.getEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class DebugDeviceLocator(app: Application) : DeviceLocator {

  enum class Mode { REAL, MOCK }

  private val preferenceStore = app.debugPreferences
  private val realLocator = RealDeviceLocator(app)

  private val modeFlow: StateFlow<Mode>
  private val locationNameFlow: StateFlow<String>

  init {
    val mode = preferenceStore.blockingGet().getEnum(DEVICE_LOCATION_MODE) ?: Mode.REAL
    val locationName = preferenceStore.blockingGetValue(DEVICE_LOCATION) ?: mockLocations.entries.first().key
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    modeFlow = preferenceStore.data
      .map { preferences -> preferences.getEnum(DEVICE_LOCATION_MODE) ?: Mode.REAL }
      .stateIn(scope, SharingStarted.Eagerly, mode)

    locationNameFlow = preferenceStore.data
      .map { preferences -> preferences[DEVICE_LOCATION] ?: mockLocations.entries.first().key }
      .stateIn(scope, SharingStarted.Eagerly, locationName)
  }

  override fun enable() {
    if (modeFlow.value == Mode.REAL) realLocator.enable()
  }

  override fun disable() {
    if (modeFlow.value == Mode.REAL) realLocator.disable()
  }

  override fun observeDeviceLocation(): Flow<DeviceLocation> {
    return modeFlow.flatMapLatest { mode ->
      when (mode) {
        Mode.REAL -> realLocator.observeDeviceLocation()
        Mode.MOCK -> locationNameFlow.map { mockLocations.getValue(it) }.onEach { println("Emitting $it") }
      }
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
