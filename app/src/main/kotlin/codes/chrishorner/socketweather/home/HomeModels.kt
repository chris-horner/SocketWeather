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

enum class HomeEvent { ChooseLocation, Refresh, ViewAbout }

data class FormattedConditions(
  val iconDescriptor: String,
  val isNight: Boolean,
  val currentTemperature: String,
  val highTemperature: String,
  val lowTemperature: String,
  val feelsLikeTemperature: String,
  val description: String?,
)
