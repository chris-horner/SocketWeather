package codes.chrishorner.socketweather.data

import android.app.Application
import au.com.gridstone.debugdrawer.okhttplogs.HttpLogger
import au.com.gridstone.debugdrawer.retrofit.DebugRetrofitConfig
import au.com.gridstone.debugdrawer.retrofit.Endpoint
import codes.chrishorner.socketweather.debug.DebugEndpoint
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.ENDPOINT
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_DELAY
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_ERROR_RATE
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_FAIL_RATE
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_VARIANCE
import codes.chrishorner.socketweather.debug.debugPreferences
import com.squareup.moshi.Moshi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Logger
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DebugNetworkComponents(
  app: Application,
  apiEndpoint: String,
  moshi: Moshi
) : NetworkComponents {

  @Deprecated("Delete once migration to Compose is complete.")
  val debugRetrofitConfig: DebugRetrofitConfig
  @Deprecated("Delete once migration to Compose is complete.")
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

  private val preferenceStore = app.debugPreferences
  override val api: WeatherApi
  override val environmentChanges: Flow<Unit> = preferenceStore.data
    .map { it[ENDPOINT] }
    .distinctUntilChanged()
    .drop(1)
    .map { }

  init {
    val endpoints = listOf(
      Endpoint("Mock", "https://localhost/mock/", isMock = true),
      Endpoint("Production", apiEndpoint)
    )
    val networkBehavior = NetworkBehavior.create()
    debugRetrofitConfig = DebugRetrofitConfig(app, endpoints, networkBehavior)

    val httpClient: OkHttpClient = OkHttpClient.Builder()
      .addInterceptor(httpLogger2)
      .build()

    val currentEndpoint: DebugEndpoint = runBlocking {
      preferenceStore.data
        .map { preferences -> preferences[ENDPOINT] ?: DebugEndpoint.MOCK.ordinal }
        .map { index -> DebugEndpoint.values()[index] }
        .first()
    }

    val retrofit: Retrofit = Retrofit.Builder()
      .baseUrl(
        when (currentEndpoint) {
          DebugEndpoint.MOCK -> "https://localhost/mock/"
          DebugEndpoint.PRODUCTION -> apiEndpoint
        }
      )
      .client(httpClient)
      .addConverterFactory(EnvelopeConverter)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()

    api = when (currentEndpoint) {
      DebugEndpoint.MOCK -> MockRetrofit.Builder(retrofit)
        .networkBehavior(networkBehavior)
        .build()
        .let { MockWeatherApi(it) }
      DebugEndpoint.PRODUCTION -> retrofit.create()
    }

    preferenceStore.data
      .onEach { preferences ->
        networkBehavior.setDelay(preferences[MOCK_HTTP_DELAY] ?: 1_000, TimeUnit.MILLISECONDS)
        networkBehavior.setVariancePercent(preferences[MOCK_HTTP_VARIANCE] ?: 40)
        networkBehavior.setFailurePercent(preferences[MOCK_HTTP_FAIL_RATE] ?: 0)
        networkBehavior.setErrorPercent(preferences[MOCK_HTTP_ERROR_RATE] ?: 0)
        networkBehavior.setErrorFactory {
          val errorCode = preferences[DebugPreferenceKeys.MOCK_HTTP_ERROR_CODE] ?: 500
          return@setErrorFactory Response.error<Any?>(errorCode, ByteArray(0).toResponseBody(null))
        }
      }
      .launchIn(MainScope())
  }
}
