package codes.chrishorner.socketweather.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object DataConfig {

  const val API_ENDPOINT = "https://api.weather.bom.gov.au/v1/"

  val moshi: Moshi = Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
}
