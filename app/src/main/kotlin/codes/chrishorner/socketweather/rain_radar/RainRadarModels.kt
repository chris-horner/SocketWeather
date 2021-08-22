package codes.chrishorner.socketweather.rain_radar

import java.time.ZoneId

data class RainRadarState(
  val subtitle: String = "",
  val timestamps: List<String> = emptyList(),
  val activeOverlayIndex: Int = 0,
)

// TODO: Parcelise and use this as an arg for Rain Radar screen.
data class RainRadarLocation(
  val latitude: Double,
  val longitude: Double,
  val timezone: ZoneId,
)
