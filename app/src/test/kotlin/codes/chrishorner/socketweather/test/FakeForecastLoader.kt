package codes.chrishorner.socketweather.test

import codes.chrishorner.socketweather.data.ForecastLoader
import codes.chrishorner.socketweather.data.ForecastLoader.State
import codes.chrishorner.socketweather.data.ForecastLoader.State.Idle
import kotlinx.coroutines.flow.MutableStateFlow

class FakeForecastLoader : ForecastLoader {

  val refreshCalls = TestChannel<Unit>()
  override val states = MutableStateFlow<State>(Idle)

  override fun refreshIfNecessary() = Unit

  override fun forceRefresh() {
    refreshCalls.send(Unit)
  }
}
