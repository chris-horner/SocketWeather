package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context.MODE_PRIVATE
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
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
  private val modeChannel: ConflatedBroadcastChannel<Mode>
  private val locationNameChannel: ConflatedBroadcastChannel<String>

  val locationNames: List<String> = mockLocations.keys.toList()

  val currentMode: Mode
    get() = modeChannel.value

  val currentLocationName: String
    get() = locationNameChannel.value

  init {
    val mode = Mode.values()[sharedPrefs.getInt("mode", Mode.REAL.ordinal)]
    val locationName = sharedPrefs.getString("locationName", null) ?: "Melbourne"
    modeChannel = ConflatedBroadcastChannel(mode)
    locationNameChannel = ConflatedBroadcastChannel(locationName)
  }

  fun setMode(mode: Mode) {
    if (mode == modeChannel.value) return
    sharedPrefs.edit().putInt("mode", mode.ordinal).apply()
    if (modeChannel.value == Mode.REAL) realLocator.disable()
    modeChannel.offer(mode)
  }

  fun setLocation(name: String) {
    if (name == locationNameChannel.value) return
    sharedPrefs.edit().putString("locationName", name).apply()
    locationNameChannel.offer(name)
  }

  override fun enable() {
    if (modeChannel.value == Mode.REAL) realLocator.enable()
  }

  override fun disable() {
    if (modeChannel.value == Mode.REAL) realLocator.disable()
  }

  override fun observeDeviceLocation(): Flow<DeviceLocation> {
    return modeChannel.asFlow().flatMapLatest { mode ->
      when (mode) {
        Mode.REAL -> realLocator.observeDeviceLocation()
        Mode.MOCK -> locationNameChannel.asFlow().map { mockLocations.getValue(it) }
      }
    }
  }
}
