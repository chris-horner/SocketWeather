package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.data.Forecaster.State
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class HomeViewModel(private val forecaster: Forecaster, private val clock: Clock) {

  fun observeStates(): Flow<State> = forecaster.states

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
}
