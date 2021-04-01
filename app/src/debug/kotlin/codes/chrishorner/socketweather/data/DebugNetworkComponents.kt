package codes.chrishorner.socketweather.data

import android.app.Application
import au.com.gridstone.debugdrawer.okhttplogs.HttpLogger
import au.com.gridstone.debugdrawer.retrofit.DebugRetrofitConfig
import au.com.gridstone.debugdrawer.retrofit.Endpoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

class DebugNetworkComponents(app: Application) : NetworkComponents {

  private val endpointChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  val debugRetrofitConfig: DebugRetrofitConfig
  val httpLogger = HttpLogger(app, prettyPrintJson = true)

  override val api: WeatherApi
  override val environmentChanges: Flow<Unit> = endpointChanges

  init {
    val endpoints = listOf(
      Endpoint("Mock", "https://localhost/mock/", isMock = true),
      Endpoint("Production", DataConfig.API_ENDPOINT)
    )
    val networkBehavior = NetworkBehavior.create()
    debugRetrofitConfig = DebugRetrofitConfig(app, endpoints, networkBehavior)
    debugRetrofitConfig.doOnEndpointChange { _, _ ->
      endpointChanges.tryEmit(Unit)
    }

    val httpClient: OkHttpClient = OkHttpClient.Builder()
      .addInterceptor(httpLogger.interceptor)
      .build()

    val currentEndpoint = debugRetrofitConfig.currentEndpoint
    val retrofit: Retrofit = Retrofit.Builder()
      .baseUrl(currentEndpoint.url)
      .client(httpClient)
      .addConverterFactory(EnvelopeConverter)
      .addConverterFactory(MoshiConverterFactory.create(DataConfig.moshi))
      .build()

    api = if (currentEndpoint.isMock) {
      MockRetrofit.Builder(retrofit)
        .networkBehavior(networkBehavior)
        .build()
        .let { MockWeatherApi(it) }
    } else {
      retrofit.create()
    }
  }
}
