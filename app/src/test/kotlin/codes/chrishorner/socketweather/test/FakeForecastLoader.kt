package codes.chrishorner.socketweather.test

import app.cash.turbine.Turbine
import codes.chrishorner.socketweather.data.ForecastLoader
import codes.chrishorner.socketweather.data.ForecastLoader.Result
import codes.chrishorner.socketweather.data.ForecastLoader.State
import codes.chrishorner.socketweather.data.ForecastLoader.State.Idle
import kotlinx.coroutines.flow.MutableStateFlow

class FakeForecastLoader : ForecastLoader {

  val refreshCalls = Turbine<Unit>()
  override val states = MutableStateFlow<State>(Idle)

  override fun refreshIfNecessary() = Unit

  override fun forceRefresh() {
    refreshCalls.add(Unit)
  }

  override suspend fun synchronousRefresh(): Result = Result.Success
}
