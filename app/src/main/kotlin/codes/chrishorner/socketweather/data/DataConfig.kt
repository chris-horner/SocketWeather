package codes.chrishorner.socketweather.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonReader.Token
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import java.lang.reflect.Type

object DataConfig {

  const val API_ENDPOINT = "https://api.weather.bom.gov.au/v1/"

  val moshi: Moshi = Moshi.Builder()
      .add(InstantAdapter)
      .add(ZoneIdAdapter)
      .add(LocationSelectionAdapter)
      .add(SkipBadElementsListAdapterFactory)
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
  private object LocationSelectionAdapter {

    @ToJson fun toJson(
        writer: JsonWriter,
        value: LocationSelection,
        stringAdapter: JsonAdapter<String>,
        staticAdapter: JsonAdapter<LocationSelection.Static>
    ) {
      when (value) {
        is LocationSelection.None -> stringAdapter.toJson(writer, "None")
        is LocationSelection.FollowMe -> stringAdapter.toJson(writer, "FollowMe")
        is LocationSelection.Static -> staticAdapter.toJson(writer, value)
      }
    }

    @FromJson fun fromJson(
        reader: JsonReader,
        staticAdapter: JsonAdapter<LocationSelection.Static>
    ): LocationSelection {

      val selection: LocationSelection? = when {
        reader.peek() == Token.BEGIN_OBJECT -> staticAdapter.fromJson(reader)
        reader.nextString() == "FollowMe" -> LocationSelection.FollowMe
        else -> LocationSelection.None
      }

      return selection ?: throw JsonDataException("Failed to deserialize SelectedLocation.")
    }
  }

  /**
   * BOM sometimes sends down completely invalid objects in lists. This factory creates
   * adapters that skip invalid elements.
   */
  private object SkipBadElementsListAdapterFactory : JsonAdapter.Factory {

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
      if (annotations.isNotEmpty() || Types.getRawType(type) != List::class.java) {
        return null
      }

      val elementType = Types.collectionElementType(type, List::class.java)
      val elementAdapter = moshi.adapter<Any>(elementType)
      return SkipBadElementsListAdapter(elementAdapter)
    }

    private class SkipBadElementsListAdapter(
        private val elementAdapter: JsonAdapter<Any>
    ) : JsonAdapter<List<Any>>() {

      override fun fromJson(reader: JsonReader): List<Any> {
        val result = mutableListOf<Any>()
        reader.beginArray()
        while (reader.hasNext()) {
          try {
            val peeked = reader.peekJson()
            result += elementAdapter.fromJson(peeked)!!
          } catch (ignored: JsonDataException) {
            // Skip bad element.
          }
          reader.skipValue()
        }
        reader.endArray()
        return result
      }

      override fun toJson(writer: JsonWriter, value: List<Any>?) {
        if (value == null) {
          throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginArray()
        for (i in value.indices) {
          elementAdapter.toJson(writer, value[i])
        }
        writer.endArray()
      }
    }
  }
}
