package codes.chrishorner.socketweather.home

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

enum class HomeEvent { ChooseLocation, Refresh, ViewAbout }
