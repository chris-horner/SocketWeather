package codes.chrishorner.socketweather.data

import kotlinx.coroutines.flow.Flow

interface NetworkComponents {

  val api: WeatherApi

  /**
   * Emits whenever there's a fundamental change in environment config.
   * (Like changing between mock and real endpoints in debug builds).
   */
  val environmentChanges: Flow<Unit>
}
