package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.DataConfig.UserAgentInterceptor
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class ReleaseNetworkComponents(apiEndpoint: String, moshi: Moshi) : NetworkComponents {

  override val api: WeatherApi = Retrofit.Builder()
    .baseUrl(apiEndpoint)
    .client(
      OkHttpClient.Builder()
        .addNetworkInterceptor(UserAgentInterceptor())
        .build()
    )
    .addConverterFactory(EnvelopeConverter)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()
    .create()

  // Environment never changes in release builds.
  override fun addEnvironmentChangeAction(action: () -> Unit) = Unit
}
