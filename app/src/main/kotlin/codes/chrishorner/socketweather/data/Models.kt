package codes.chrishorner.socketweather.data

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

sealed class LocationSelection {
  object FollowMe : LocationSelection()
  data class Static(val location: Location) : LocationSelection()
  object None : LocationSelection()
}

data class Forecast(
    val updateTime: Instant,
    val location: Location,
    val currentTemp: Float,
    val tempFeelsLike: Float,
    val highTemp: Int,
    val lowTemp: Int,
    val dateForecasts: List<DateForecast>
)

data class DeviceLocation(val latitude: Double, val longitude: Double)

/**
 * A representation of the wrapped payloads the BOM API returns. Any Envelope objects
 * returned by the API will be unwrapped by [EnvelopeConverter].
 */
data class Envelope<T>(val data: T)

data class SearchResult(
    val id: String,
    val geohash: String,
    val name: String,
    val postcode: String?,
    val state: String
)

data class Location(
    val id: String,
    val geohash: String,
    val name: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: ZoneId
)

data class CurrentObservations(
    val temp: Float,
    val temp_feels_like: Float,
    val station: Station
)

data class Station(val name: String, val distance: Int)

data class DateForecast(
    val date: Instant,
    val temp_min: Int?,
    val temp_max: Int,
    val extended_text: String?,
    val short_text: String?,
    val icon_descriptor: String,
    val rain: Rain,
    val now: CurrentInformation? = null
)

data class Rain(val amount: Amount, val chance: Int) {
  data class Amount(val min: Float?, val max: Float?, val units: String)
}

data class CurrentInformation(
    val is_night: Boolean,
    val now_label: String,
    val later_label: String,
    val temp_now: Int,
    val temp_later: Int
)

data class ThreeHourlyForecast(
    val rain: Rain,
    val temp: Int,
    val icon_descriptor: String,
    val time: Instant,
    val is_night: Boolean
)
