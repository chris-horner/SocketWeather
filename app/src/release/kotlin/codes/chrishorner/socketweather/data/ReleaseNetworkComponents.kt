package codes.chrishorner.socketweather.data

import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
  override val environmentChanges: Flow<Unit> = emptyFlow()
}
