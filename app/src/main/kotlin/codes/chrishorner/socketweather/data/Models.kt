package codes.chrishorner.socketweather.data

/**
 * A representation of the wrapped payloads the BOM API returns. Any Envelope objects
 * returned by the API will be unwrapped by [EnvelopeConverter].
 */
data class Envelope<T>(val data: T)

data class Location(
    val id: String,
    val geohash: String,
    val name: String,
    val postcode: String,
    val state: String
)
