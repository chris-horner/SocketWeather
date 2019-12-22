package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class NetworkComponents private constructor(
    @Suppress("UNUSED_PARAMETER") app: Application // Match signature of debug variant.
) {

  val api: WeatherApi = Retrofit.Builder()
      .baseUrl(DataConfig.API_ENDPOINT)
      .addConverterFactory(MoshiConverterFactory.create(DataConfig.moshi))
      .addConverterFactory(EnvelopeConverter)
      .build()
      .create()

  companion object {
    private var instance: NetworkComponents? = null

    @Synchronized fun from(context: Context): NetworkComponents {
      instance?.let { return it }
      return NetworkComponents(context.applicationContext as Application).also { instance = it }
    }
  }
}
