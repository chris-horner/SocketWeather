package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.data.ForecastState
import codes.chrishorner.socketweather.data.Forecaster
import kotlinx.coroutines.flow.Flow

class HomeViewModel(private val forecaster: Forecaster) {

  fun observeStates(): Flow<ForecastState> = forecaster.observeForecasts()

  fun forceRefresh() {
    forecaster.refresh()
  }
}
