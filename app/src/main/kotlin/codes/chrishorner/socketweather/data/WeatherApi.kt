package codes.chrishorner.socketweather.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {

  @GET("locations")
  suspend fun searchForLocation(@Query("search") query: String): List<SearchResult>

  @GET("locations/{geohash}")
  suspend fun getLocation(@Path("geohash") geohash: String): Location

  @GET("locations/{geohash}/observations")
  suspend fun getObservations(@Path("geohash") geohash: String): CurrentObservations

  @GET("locations/{geohash}/forecasts/daily")
  suspend fun getDateForecasts(@Path("geohash") geohash: String): List<DateForecast>

  @GET("locations/{geohash}/forecasts/hourly")
  suspend fun getHourlyForecasts(@Path("geohash") geohash: String): List<HourlyForecast>
}
