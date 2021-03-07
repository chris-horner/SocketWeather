package codes.chrishorner.socketweather.data

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import org.junit.Test
import org.threeten.bp.ZoneId

class JsonParsingTest {

  private val adapter: JsonAdapter<LocationSelection> = DataConfig.moshi.adapter(LocationSelection::class.java)

  @Test fun `FollowMe LocationSelection serialises and deserialises`() {
    val json = adapter.toJson(LocationSelection.FollowMe)
    val deserialised: LocationSelection? = adapter.fromJson(json)
    assert(deserialised is LocationSelection.FollowMe)
    assertThat(deserialised).isEqualTo(LocationSelection.FollowMe)
  }

  @Test fun `Static LocationSelection serialises and deserialises`() {
    val location = Location(
      id = "Fakezroy-r1r0gnd",
      geohash = "r1r0gnd",
      name = "Fakezroy",
      state = "VIC",
      latitude = -37.80052185058594,
      longitude = 144.97901916503906,
      timezone = ZoneId.of("Australia/Melbourne")
    )

    val selectedLocation = LocationSelection.Static(location)
    val json = adapter.toJson(selectedLocation)
    val deserialised: LocationSelection? = adapter.fromJson(json)
    assertThat(deserialised).isEqualTo(selectedLocation)
  }

  @Test fun `None LocationSelection serialises and deserialises`() {
    val json = adapter.toJson(LocationSelection.None)
    val deserialised: LocationSelection? = adapter.fromJson(json)
    assertThat(deserialised).isEqualTo(LocationSelection.None)
  }
}
