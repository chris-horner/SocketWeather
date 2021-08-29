package codes.chrishorner.socketweather.rain_radar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.chrishorner.socketweather.appSingletons
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
import java.time.ZoneId

class RainRadarViewModel(
  location: RainRadarLocation,
  clock: Clock = Clock.systemDefaultZone(),
  overrideScope: CoroutineScope? = null,
) : ViewModel() {

  private val scope = overrideScope ?: viewModelScope

  val states: StateFlow<RainRadarState> = tickerFlow(Duration.ofMinutes(1).toMillis(), emitImmediately = true)
    // Every minute, generate a new list of rain radar timestamps.
    .map { generateRainRadarTimestamps(clock) }
    .transformLatest { rainTimestamps ->
      val subtitles = rainTimestamps.map { it.label }
      val state = RainRadarState(location = location, timestamps = rainTimestamps.map { it.timestamp })
      var activeIndex = 0

      // Loop through and display each rainfall overlay.
      while (true) {
        emit(state.copy(subtitle = subtitles[activeIndex], activeTimestampIndex = activeIndex))

        // Pause on each timestamp for 500ms, or 1s if it's the last.
        if (activeIndex == rainTimestamps.size - 1) delay(1_000) else delay(500)

        activeIndex++
        if (activeIndex >= rainTimestamps.size) activeIndex = 0
      }
    }
    .stateIn(scope, SharingStarted.WhileSubscribed(5_000), RainRadarState(location))

  companion object {

    // Centred on Australia.
    private val defaultLocation = RainRadarLocation(
      latitude = -25.976012,
      longitude = 134.145419,
      timezone = ZoneId.systemDefault(),
      zoom = 3.0,
    )

    operator fun invoke(context: Context): RainRadarViewModel {
      val forecast = context.appSingletons.forecaster.forecast.value
      val location = forecast?.location
        ?.let { RainRadarLocation(it.latitude, it.longitude, it.timezone, zoom = 9.0) }
        ?: defaultLocation

      return RainRadarViewModel(location)
    }
  }
}
