package codes.chrishorner.socketweather.data

import com.squareup.moshi.JsonAdapter
import org.junit.Test
import org.threeten.bp.ZoneId

class JsonParsingTest {

  private val adapter: JsonAdapter<SelectedLocation> = DataConfig.moshi.adapter(SelectedLocation::class.java)

  @Test fun `FollowMe SelectedLocation serialises and deserialises`() {
    val json = adapter.toJson(SelectedLocation.FollowMe)
    val deserialised: SelectedLocation? = adapter.fromJson(json)
    assert(deserialised is SelectedLocation.FollowMe)
  }

  @Test fun `Static SelectedLocation serialises and deserialises`() {
    val location = Location(
        id = "Fakezroy-r1r0gnd",
        geohash = "r1r0gnd",
        name = "Fakezroy",
        state = "VIC",
        latitude = -37.80052185058594,
        longitude = 144.97901916503906,
        timezone = ZoneId.of("Australia/Melbourne")
    )

    val selectedLocation = SelectedLocation.Static(location)
    val json = adapter.toJson(selectedLocation)
    val deserialised: SelectedLocation? = adapter.fromJson(json)
    assert(deserialised == selectedLocation)
  }
}
