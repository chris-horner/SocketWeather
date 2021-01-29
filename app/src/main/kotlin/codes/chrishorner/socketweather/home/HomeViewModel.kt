package codes.chrishorner.socketweather.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.data.Forecaster.State
import codes.chrishorner.socketweather.home.HomeState.RefreshTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class HomeViewModel(
    private val forecaster: Forecaster,
    private val clock: Clock = Clock.systemDefaultZone()
) : ViewModel() {

  val states: StateFlow<State> = forecaster.states

  val states2: StateFlow<HomeState> = forecaster.states
      .map { it.toHomeState() }
      .stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = forecaster.states.value.toHomeState())

  fun forceRefresh() {
    forecaster.refresh()
  }

  fun refreshIfNecessary() {
    // Refresh the forecast if we don't currently have one, or if the current forecast
    // is more than 1 minute old.
    when (val state = forecaster.states.value) {

      is State.Idle -> forecaster.refresh()

      is State.Loaded -> {
        val elapsedTime = Duration.between(state.forecast.updateTime, Instant.now(clock))
        if (elapsedTime.toMinutes() > 1) {
          forecaster.refresh()
        }
      }
    }
  }

  private fun Forecaster.State.toHomeState(): HomeState {
    val refreshTime = when (this) {
      is Forecaster.State.Refreshing -> RefreshTime.InProgress
      is Forecaster.State.Loaded -> {
        if (Duration.between(clock.instant(), forecast.updateTime).toMinutes() > 0) {
          RefreshTime.TimeAgo(forecast.updateTime)
        } else {
          RefreshTime.JustNow
        }
      }
      else -> RefreshTime.Failed
    }

    return HomeState(refreshTime, this)
  }
}
