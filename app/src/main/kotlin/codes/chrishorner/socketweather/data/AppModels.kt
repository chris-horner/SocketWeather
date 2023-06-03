package codes.chrishorner.socketweather.data

import com.squareup.moshi.JsonClass
import dev.zacsweers.moshix.sealed.annotations.TypeLabel
import java.time.Instant

@JsonClass(generateAdapter = true, generator = "sealed:type")
sealed class LocationSelection {
  @TypeLabel("FollowMe")
  object FollowMe : LocationSelection()
  @TypeLabel("Static")
  @JsonClass(generateAdapter = true)
  data class Static(val location: Location) : LocationSelection()
  @TypeLabel("None")
  object None : LocationSelection()
}

@JsonClass(generateAdapter = true)
data class Forecast(
  val updateTime: Instant,
  val location: Location,
  val iconDescriptor: String,
  val night: Boolean,
  val currentTemp: Float,
  val tempFeelsLike: Float?,
  val humidity: Int?,
  val wind: Wind,
  val highTemp: Int,
  val lowTemp: Int,
  val todayForecast: DateForecast,
  val hourlyForecasts: List<HourlyForecast>,
  val upcomingForecasts: List<DateForecast>
)

@JsonClass(generateAdapter = true)
data class DeviceLocation(val latitude: Double, val longitude: Double)

enum class ForecastError {
  /**
   * Nothing went wrong determining the location or making a request to the API, but it
   * responded with something malformed.
   */
  DATA,
  /**
   * This means a communication error occurred between the app and API.
   */
  NETWORK,
  /**
   * Something went wrong resolving the location for the forecast.
   */
  LOCATION,
  /**
   * Tried to load a forecast using a location that's outside of Australia.
   */
  NOT_AUSTRALIA,
}

@JsonClass(generateAdapter = true)
data class RainTimestamp(val timestamp: String, val label: String)

@JsonClass(generateAdapter = true)
data class Settings(
  val useDynamicColors: Boolean = false,
)
