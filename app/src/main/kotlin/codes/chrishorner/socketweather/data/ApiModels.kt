package codes.chrishorner.socketweather.data

import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

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
    val temp_feels_like: Float?,
    val wind: Wind,
    val humidity: Int?,
    val station: Station
)

data class Station(val name: String)

data class DateForecast(
    val date: Instant,
    val temp_min: Int?,
    val temp_max: Int,
    val extended_text: String?,
    val short_text: String?,
    val icon_descriptor: String,
    val rain: Rain,
    val uv: Uv,
    val now: CurrentInformation? = null
)

data class Rain(val amount: Amount, val chance: Int) {
  data class Amount(val min: Float?, val max: Float?, val units: String)
}

data class Wind(val speed_kilometre: Int, val direction: String)

data class Uv(val max_index: Int?, val start_time: Instant?, val end_time: Instant?)

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
    val wind: Wind,
    val icon_descriptor: String,
    val time: Instant,
    val is_night: Boolean
)
