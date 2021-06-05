package codes.chrishorner.socketweather.data

import com.squareup.moshi.Moshi
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class ReleaseNetworkComponents(apiEndpoint: String, moshi: Moshi) : NetworkComponents {

  override val api: WeatherApi = Retrofit.Builder()
    .baseUrl(apiEndpoint)
    .addConverterFactory(EnvelopeConverter)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()
    .create()

  // Environment never changes in release builds.
  override fun addEnvironmentChangeAction(action: () -> Unit) = Unit
}
