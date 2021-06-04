package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.data.ForecastError
import codes.chrishorner.socketweather.data.LocationSelection

data class HomeState(
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
  val selection: LocationSelection,
  val title: String,
  val subtitle: String,
  val showTrackingIcon: Boolean = false
)

sealed class HomeEvent {
  data class SwitchLocation(val selection: LocationSelection) : HomeEvent()
  object AddLocation : HomeEvent()
  object Refresh : HomeEvent()
  object ViewAbout : HomeEvent()
}

data class FormattedConditions(
  val iconDescriptor: String,
  val isNight: Boolean,
  val currentTemperature: String,
  val highTemperature: String,
  val lowTemperature: String,
  val feelsLikeTemperature: String,
  val humidityPercent: String?,
  val windSpeed: String,
  val uvWarningTimes: String?,
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
