package codes.chrishorner.socketweather.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext", "FunctionName") // https://youtrack.jetbrains.com/issue/KTIJ-838
inline fun <reified T> MoshiSerializer(moshi: Moshi, default: T): Serializer<T> = object : Serializer<T> {

  private val adapter: JsonAdapter<T> = moshi.adapter()

  override val defaultValue: T = default

  override suspend fun readFrom(input: InputStream): T {
    return try {
      withContext(Dispatchers.IO) {
        input.source().buffer().use { source ->
          if (!source.exhausted()) adapter.fromJson(source) else null
        } ?: defaultValue
      }
    } catch (e: Exception) {
      Timber.e(e, "Failed to read value from disk.")
      defaultValue
    }
  }

  override suspend fun writeTo(t: T, output: OutputStream) {
    try {
      withContext(Dispatchers.IO) {
        output.sink().buffer().use { sink -> adapter.toJson(sink, t) }
      }
    } catch (e: JsonDataException) {
      throw CorruptionException("Failed to write to store.", e)
    }
  }
}
