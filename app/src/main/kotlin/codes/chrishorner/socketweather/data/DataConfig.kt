package codes.chrishorner.socketweather.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

object DataConfig {

  const val API_ENDPOINT = "https://api.weather.bom.gov.au/v1/"

  val moshi: Moshi = Moshi.Builder()
      .add(InstantAdapter)
      .add(ZoneIdAdapter)
      .add(SelectedLocationAdapter)
      .add(KotlinJsonAdapterFactory())
      .build()

  private object InstantAdapter {
    @ToJson fun toJson(value: Instant) = value.toString()
    @FromJson fun fromJson(value: String): Instant = Instant.parse(value)
  }

  private object ZoneIdAdapter {
    @ToJson fun toJson(value: ZoneId) = value.toString()
    @FromJson fun fromJson(value: String): ZoneId = ZoneId.of(value)
  }

  /**
   * Moshi doesn't handle Kotlin sealed classes automatically. If we need to serialise
   * more of these in the future it might be worth using the `moshi-sealed` library,
   * but for now we'll parse this one type by hand.
   */
  private object SelectedLocationAdapter {

    @ToJson fun toJson(
        writer: JsonWriter,
        value: SelectedLocation,
        followMeAdapter: JsonAdapter<String>,
        staticAdapter: JsonAdapter<SelectedLocation.Static>
    ) {
      when (value) {
        is SelectedLocation.FollowMe -> followMeAdapter.toJson(writer, "FollowMe")
        is SelectedLocation.Static -> staticAdapter.toJson(writer, value)
      }
    }

    @FromJson fun fromJson(
        reader: JsonReader,
        staticAdapter: JsonAdapter<SelectedLocation.Static>
    ): SelectedLocation {

      val selectedLocation = if (reader.peek() == Token.BEGIN_OBJECT) {
        staticAdapter.fromJson(reader)
      } else {
        SelectedLocation.FollowMe.also { reader.skipValue() }
      }

      return selectedLocation ?: throw JsonDataException("Failed to deserialize SelectedLocation.")
    }
  }
}
