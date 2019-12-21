package codes.chrishorner.socketweather.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {

  @GET("locations")
  suspend fun searchForLocation(@Query("search") query: String): List<Location>

  @GET("locations?search={lat},{lng}")
  suspend fun searchForLocation(@Path("lat") latitude: Double, @Path("lng") longitude: Double): List<Location>
}
