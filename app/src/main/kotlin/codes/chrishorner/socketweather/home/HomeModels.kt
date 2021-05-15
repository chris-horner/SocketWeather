package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.data.ForecastError
import codes.chrishorner.socketweather.data.Forecaster
import org.threeten.bp.Instant

data class HomeState(
  val refreshTime: RefreshTime,
  val forecasterState: Forecaster.State
) {
  sealed class RefreshTime {
    object InProgress : RefreshTime()
    object JustNow : RefreshTime()
    object Failed : RefreshTime()
    data class TimeAgo(val time: Instant) : RefreshTime()
  }
}

data class HomeState2(
  val toolbarTitle: String,
  val toolbarSubtitle: String?,
  val currentLocation: LocationEntry,
  val savedLocations: List<LocationEntry>,
  val content: Content,
) {
  sealed class Content {
    object Empty : Content()
    object Loading : Content()
    data class Loaded(val conditions: FormattedConditions) : Content()
    data class Refreshing(val conditions: FormattedConditions) : Content()
    data class Error(val type: ForecastError) : Content()
  }
}

data class LocationEntry(
  val id: String,
  val title: String,
  val subtitle: String,
  val showTrackingIcon: Boolean = false
)

enum class HomeEvent { ChooseLocation, Refresh, ViewAbout }

data class FormattedConditions(
  val iconDescriptor: String,
  val isNight: Boolean,
  val currentTemperature: String,
  val highTemperature: String,
  val lowTemperature: String,
  val feelsLikeTemperature: String,
  val description: String?,
  val graphItems: List<TimeForecastGraphItem>,
  val upcomingForecasts: List<UpcomingForecast>,
)

data class TimeForecastGraphItem(
  val temperatureC: Int,
  val formattedTemperature: String,
  val time: String,
  val rainChancePercent: Int,
  val formattedRainChance: String
)

data class UpcomingForecast(
  val day: String,
  val percentChanceOfRain: Int,
  val formattedChanceOfRain: String,
  val iconDescriptor: String,
  val lowTemperature: String,
  val highTemperature: String,
)
