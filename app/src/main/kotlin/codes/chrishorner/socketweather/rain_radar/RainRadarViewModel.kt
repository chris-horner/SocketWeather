package codes.chrishorner.socketweather.rain_radar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.chrishorner.socketweather.data.generateRainRadarTimestamps
import codes.chrishorner.socketweather.util.tickerFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import java.time.Clock
import java.time.Duration

class RainRadarViewModel(
  clock: Clock = Clock.systemDefaultZone(),
  overrideScope: CoroutineScope? = null,
) : ViewModel() {

  private val scope = overrideScope ?: viewModelScope

  val states: StateFlow<RainRadarState> = tickerFlow(Duration.ofMinutes(1).toMillis(), emitImmediately = true)
    // Every minute, generate a new list of rain radar timestamps.
    .map { generateRainRadarTimestamps(clock) }
    .transformLatest { rainTimestamps ->
      val subtitles = rainTimestamps.map { it.label }
      val state = RainRadarState(timestamps = rainTimestamps.map { it.timestamp })
      var activeIndex = 0

      // Loop through and display each rainfall overlay.
      while (true) {
        emit(state.copy(subtitle = subtitles[activeIndex], activeOverlayIndex = activeIndex))

        // Pause on each timestamp for 500ms, or 1s if it's the last.
        if (activeIndex == rainTimestamps.size - 1) delay(1_000) else delay(500)

        activeIndex++
        if (activeIndex >= rainTimestamps.size) activeIndex = 0
      }
    }
    .stateIn(scope, SharingStarted.WhileSubscribed(5_000), RainRadarState())
}
