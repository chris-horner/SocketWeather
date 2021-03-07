package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context.MODE_PRIVATE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

private val mockLocations = mapOf(
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

class DebugDeviceLocator(app: Application) : DeviceLocator {

  enum class Mode { REAL, MOCK }

  private val sharedPrefs = app.getSharedPreferences("debugDeviceLocator", MODE_PRIVATE)
  private val realLocator = RealDeviceLocator(app)
  private val modeFlow: MutableStateFlow<Mode>
  private val locationNameFlow: MutableStateFlow<String>

  val locationNames: List<String> = mockLocations.keys.toList()

  var mode: Mode
    get() = modeFlow.value
    set(value) {
      if (value == modeFlow.value) return
      if (modeFlow.value == Mode.REAL) realLocator.disable()
      sharedPrefs.edit().putInt("mode", value.ordinal).apply()
      modeFlow.value = value
    }

  var locationName: String
    get() = locationNameFlow.value
    set(value) {
      if (value == locationNameFlow.value) return
      sharedPrefs.edit().putString("locationName", value).apply()
      locationNameFlow.value = value
    }

  val currentLocationName: String
    get() = locationNameFlow.value

  init {
    val mode = Mode.values()[sharedPrefs.getInt("mode", Mode.REAL.ordinal)]
    val locationName = sharedPrefs.getString("locationName", null) ?: "Melbourne"
    modeFlow = MutableStateFlow(mode)
    locationNameFlow = MutableStateFlow(locationName)
  }

  override fun enable() {
    if (mode == Mode.REAL) realLocator.enable()
  }

  override fun disable() {
    if (mode == Mode.REAL) realLocator.disable()
  }

  override fun observeDeviceLocation(): Flow<DeviceLocation> {
    return modeFlow.flatMapLatest { mode ->
      when (mode) {
        Mode.REAL -> realLocator.observeDeviceLocation()
        Mode.MOCK -> locationNameFlow.map { mockLocations.getValue(it) }
      }
    }
  }
}
