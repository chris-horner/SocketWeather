package codes.chrishorner.socketweather.rain_radar

import java.time.ZoneId

data class RainRadarState(
  val location: RainRadarLocation,
  val subtitle: String = "",
  val timestamps: List<String> = emptyList(),
  val activeTimestampIndex: Int = 0,
)

data class RainRadarLocation(
  val latitude: Double,
  val longitude: Double,
  val timezone: ZoneId,
  val zoom: Double,
)

object RainRadarBackPressEvent
