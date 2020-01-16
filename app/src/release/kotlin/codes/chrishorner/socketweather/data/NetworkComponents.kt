package codes.chrishorner.socketweather.data

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

@Suppress("UNUSED_PARAMETER") // Match signature of debug variant.
class NetworkComponents(app: Application, locationChoices: LocationChoices) {

  val api: WeatherApi = Retrofit.Builder()
      .baseUrl(DataConfig.API_ENDPOINT)
      .addConverterFactory(EnvelopeConverter)
      .addConverterFactory(MoshiConverterFactory.create(DataConfig.moshi))
      .build()
      .create()
}
