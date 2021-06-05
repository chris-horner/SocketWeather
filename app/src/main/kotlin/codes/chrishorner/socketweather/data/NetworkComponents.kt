package codes.chrishorner.socketweather.data

interface NetworkComponents {

  val api: WeatherApi

  /**
   * Listen for fundamental changes in environment config.
   * (Like changing between mock and real endpoints in debug builds).
   */
  fun addEnvironmentChangeAction(action: () -> Unit)
}
