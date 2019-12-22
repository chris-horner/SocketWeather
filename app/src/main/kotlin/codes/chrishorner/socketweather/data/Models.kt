package codes.chrishorner.socketweather.data

/**
 * BOM's API returns the data we care about wrapped in an envelope.
 */
data class Envelope<T>(val data: T)

data class Location(
    val id: String,
    val geohash: String,
    val name: String,
    val postcode: String,
    val state: String
)
