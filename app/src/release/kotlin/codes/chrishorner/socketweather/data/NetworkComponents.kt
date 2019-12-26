package codes.chrishorner.socketweather.data

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class NetworkComponents private constructor(
    @Suppress("UNUSED_PARAMETER") app: Application // Match signature of debug variant.
) {

  val api: WeatherApi = Retrofit.Builder()
      .baseUrl(DataConfig.API_ENDPOINT)
      .addConverterFactory(EnvelopeConverter)
      .addConverterFactory(MoshiConverterFactory.create(DataConfig.moshi))
      .build()
      .create()

  companion object {
    private var instance: NetworkComponents? = null

    fun init(app: Application) {
      instance = NetworkComponents(app)
    }

    fun get(): NetworkComponents {
      return requireNotNull(instance) { "NetworkComponents.init(app) must be called first." }
    }
  }
}
