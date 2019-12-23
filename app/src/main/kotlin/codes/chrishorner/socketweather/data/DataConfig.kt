package codes.chrishorner.socketweather.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

object DataConfig {

  const val API_ENDPOINT = "https://api.weather.bom.gov.au/v1/"

  val moshi: Moshi = Moshi.Builder()
      .add(InstantAdapter)
      .add(ZoneIdAdapter)
      .add(KotlinJsonAdapterFactory())
      .build()

  private object InstantAdapter {
    @ToJson fun toJson(value: Instant) = value.toString()
    @FromJson fun fromJson(value: String): Instant = Instant.parse(value)
  }

  private object ZoneIdAdapter {
    @ToJson fun toJson(value: ZoneId) = value.toString()
    @FromJson fun fromJson(value: String): ZoneId = ZoneId.of(value)
  }
}
