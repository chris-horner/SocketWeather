package codes.chrishorner.socketweather.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

class ReleaseNetworkComponents : NetworkComponents {

  override val api: WeatherApi = Retrofit.Builder()
    .baseUrl(DataConfig.API_ENDPOINT)
    .addConverterFactory(EnvelopeConverter)
    .addConverterFactory(MoshiConverterFactory.create(DataConfig.moshi))
    .build()
    .create()

  // Environment never changes in release builds.
  override val environmentChanges: Flow<Unit> = emptyFlow()
}
