package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Intent
import codes.chrishorner.socketweather.MainActivity
import codes.chrishorner.socketweather.data.DataConfig.UserAgentInterceptor
import codes.chrishorner.socketweather.debug.DebugEndpoint
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.ENDPOINT
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.HTTP_LOG_LEVEL
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_DELAY
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_ERROR_CODE
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_ERROR_RATE
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_FAIL_RATE
import codes.chrishorner.socketweather.debug.DebugPreferenceKeys.MOCK_HTTP_VARIANCE
import codes.chrishorner.socketweather.debug.blockingGet
import codes.chrishorner.socketweather.debug.debugPreferences
import codes.chrishorner.socketweather.debug.getEnum
import com.squareup.moshi.Moshi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
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

  private val logger = HttpLoggingInterceptor { message ->
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

  private val preferenceStore = app.debugPreferences
  override val api: WeatherApi

  private var environmentChangeAction = {}

  init {
    val networkBehavior = NetworkBehavior.create()

    val httpClient: OkHttpClient = OkHttpClient.Builder()
      .addInterceptor(logger)
      .addNetworkInterceptor(UserAgentInterceptor())
      .build()

    val currentEndpoint = preferenceStore.blockingGet().getEnum(ENDPOINT) ?: DebugEndpoint.MOCK

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

    // Create a never ending scope for this component. It lives for the duration of the process.
    val scope = MainScope()

    preferenceStore.data
      .onEach { preferences ->
        logger.level = preferences.getEnum<Level>(HTTP_LOG_LEVEL) ?: Level.BASIC
        networkBehavior.setDelay(preferences[MOCK_HTTP_DELAY] ?: 1_000, TimeUnit.MILLISECONDS)
        networkBehavior.setVariancePercent(preferences[MOCK_HTTP_VARIANCE] ?: 40)
        networkBehavior.setFailurePercent(preferences[MOCK_HTTP_FAIL_RATE] ?: 0)
        networkBehavior.setErrorPercent(preferences[MOCK_HTTP_ERROR_RATE] ?: 0)
        networkBehavior.setErrorFactory {
          val errorCode = preferences[MOCK_HTTP_ERROR_CODE] ?: 500
          return@setErrorFactory Response.error<Any?>(errorCode, ByteArray(0).toResponseBody(null))
        }
      }
      .launchIn(scope)

    scope.launch {
      // Await the endpoint changing.
      preferenceStore.data
        .map { it[ENDPOINT] }
        .distinctUntilChanged()
        .drop(1)
        .first()

      // Notify any listeners.
      environmentChangeAction()

      // Restart the app.
      val intent = Intent(app, MainActivity::class.java)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
      app.startActivity(intent)
      Runtime.getRuntime().exit(0)
    }
  }

  override fun addEnvironmentChangeAction(action: () -> Unit) {
    environmentChangeAction = action
  }
}
