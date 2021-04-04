package codes.chrishorner.socketweather.data

import android.app.Application
import au.com.gridstone.debugdrawer.okhttplogs.HttpLogger
import au.com.gridstone.debugdrawer.retrofit.DebugRetrofitConfig
import au.com.gridstone.debugdrawer.retrofit.Endpoint
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Logger
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import timber.log.Timber

class DebugNetworkComponents(
  app: Application,
  apiEndpoint: String,
  moshi: Moshi
) : NetworkComponents {

  private val endpointChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

  val debugRetrofitConfig: DebugRetrofitConfig
  val httpLogger = HttpLogger(app, prettyPrintJson = true)
  val httpLogger2 = HttpLoggingInterceptor(object : Logger {
    override fun log(message: String) {
      val formattedMessage: String = try {
        when {
          message.startsWith('{') -> JSONObject(message).toString(2)
          message.startsWith('[') -> JSONArray(message).toString(2)
          else -> message
        }
      } catch (e: JSONException) {
        message
      }

      Timber.tag("HTTP").v(formattedMessage)
    }
  })

  override val api: WeatherApi
  override val environmentChanges: Flow<Unit> = endpointChanges

  init {
    val endpoints = listOf(
      Endpoint("Mock", "https://localhost/mock/", isMock = true),
      Endpoint("Production", apiEndpoint)
    )
    val networkBehavior = NetworkBehavior.create()
    debugRetrofitConfig = DebugRetrofitConfig(app, endpoints, networkBehavior)
    debugRetrofitConfig.doOnEndpointChange { _, _ ->
      endpointChanges.tryEmit(Unit)
    }

    val httpClient: OkHttpClient = OkHttpClient.Builder()
      .addInterceptor(httpLogger2)
      .build()

    val currentEndpoint = debugRetrofitConfig.currentEndpoint
    val retrofit: Retrofit = Retrofit.Builder()
      .baseUrl(currentEndpoint.url)
      .client(httpClient)
      .addConverterFactory(EnvelopeConverter)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
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
