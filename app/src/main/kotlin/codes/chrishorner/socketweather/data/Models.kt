package codes.chrishorner.socketweather.data

data class Location(
    val id: String,
    val geohash: String,
    val name: String,
    val postcode: String,
    val state: String
)
