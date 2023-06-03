package codes.chrishorner.socketweather.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.Types
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import java.lang.reflect.Type
import java.time.Instant
import java.time.ZoneId

object DataConfig {

  const val API_ENDPOINT = "https://api.weather.bom.gov.au/v1/"
  const val USER_AGENT_HEADER = "User-Agent"
  const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.169 Safari/537.36"

  class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Chain): Response = chain.proceed(
      chain.request()
        .newBuilder()
        .header(USER_AGENT_HEADER, USER_AGENT)
        .build()
    )
  }

  val moshi: Moshi = Moshi.Builder()
    .add(InstantAdapter)
    .add(ZoneIdAdapter)
    .add(SkipBadElementsListAdapterFactory)
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
