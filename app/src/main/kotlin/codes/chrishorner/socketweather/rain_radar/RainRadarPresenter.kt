package codes.chrishorner.socketweather.rain_radar

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import codes.chrishorner.socketweather.Navigator
import codes.chrishorner.socketweather.Presenter
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.data.RainTimestamp
import codes.chrishorner.socketweather.data.generateRainRadarTimestamps
import codes.chrishorner.socketweather.util.CollectEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.ZoneId
import kotlin.time.Duration.Companion.minutes

class RainRadarPresenter(
  private val navigator: Navigator,
  private val location: RainRadarLocation,
  private val clock: Clock = Clock.systemDefaultZone(),
) : Presenter<RainRadarBackPressEvent, RainRadarState> {

  @Composable
  override fun states(events: Flow<RainRadarBackPressEvent>): RainRadarState {
    var rainTimestamps by remember { mutableStateOf(generateRainRadarTimestamps(clock)) }
    val states = remember(rainTimestamps) { generateRainRadarStates(location, rainTimestamps) }

    // Every minute, generate a new list of rain radar timestamps.
    LaunchedEffect(Unit) {
      while (true) {
        delay(1.minutes)
        rainTimestamps = generateRainRadarTimestamps(clock)
      }
    }

    CollectEffect(events) { navigator.pop() }

    return states.collectAsState(getState(location, rainTimestamps)).value
  }

  private fun generateRainRadarStates(
    location: RainRadarLocation,
    rainTimestamps: List<RainTimestamp>,
  ): Flow<RainRadarState> = flow {
    val state = getState(location, rainTimestamps)
    var activeIndex = 0

    // Loop through and display each rainfall overlay.
    while (true) {
      emit(state.copy(subtitle = rainTimestamps[activeIndex].label, activeTimestampIndex = activeIndex))

      // Pause on each timestamp for 500ms, or 1s if it's the last.
      if (activeIndex == rainTimestamps.size - 1) delay(1_000) else delay(500)

      activeIndex++
      if (activeIndex >= rainTimestamps.size) activeIndex = 0
    }
  }

  private fun getState(
    location: RainRadarLocation,
    rainTimestamps: List<RainTimestamp>,
    activeIndex: Int = 0,
  ) = RainRadarState(
    location,
    rainTimestamps[activeIndex].label,
    rainTimestamps.map { it.timestamp },
    activeTimestampIndex = activeIndex
  )

  companion object {
    // Centre on Australia if there's no current location.
    private val defaultLocation = RainRadarLocation(
      latitude = -25.976012,
      longitude = 134.145419,
      timezone = ZoneId.systemDefault(),
      zoom = 3.0,
    )

    operator fun invoke(context: Context, navigator: Navigator): RainRadarPresenter {
      val location = context.appSingletons.stores.forecast.data.value?.location
        ?.let { RainRadarLocation(it.latitude, it.longitude, it.timezone, zoom = 9.0) }
        ?: defaultLocation

      return RainRadarPresenter(navigator, location)
    }
  }
}
