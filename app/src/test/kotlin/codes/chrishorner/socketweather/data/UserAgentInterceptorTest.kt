package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.DataConfig.UserAgentInterceptor
import com.google.common.truth.Truth.assertThat
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Test

class UserAgentInterceptorTest {

  private val httpClient = OkHttpClient.Builder()
    .addNetworkInterceptor(UserAgentInterceptor())
    .build()
  private val server = MockWebServer()

  @Test fun `http requests include custom user agent`() {
    server.enqueue(MockResponse())
    httpClient
      .newCall(
        Request.Builder()
          .url(server.url("/"))
          .build()
      )
      .execute()
    assertThat(server.takeRequest().getHeader(DataConfig.USER_AGENT_HEADER)).isEqualTo(DataConfig.USER_AGENT)
  }
}
