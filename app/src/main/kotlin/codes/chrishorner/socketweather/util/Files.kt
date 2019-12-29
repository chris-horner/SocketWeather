package codes.chrishorner.socketweather.util

import android.os.StrictMode
import codes.chrishorner.socketweather.data.DataConfig
import com.squareup.moshi.Types
import okio.buffer
import okio.sink
import okio.source
import java.io.File

fun getOrCreateFile(directory: File, name: String): File {
  val file = File(directory, name)
  if (!file.exists()) file.createNewFile()
  return file
}

inline fun <reified T> File.readValue(): T? {
  source().buffer().use { source ->
    if (source.exhausted()) return null
    val adapter = DataConfig.moshi.adapter(T::class.java)
    return adapter.fromJson(source)
  }
}

inline fun <reified T> File.writeValue(value: T) {
  val adapter = DataConfig.moshi.adapter(T::class.java)
  sink().buffer().use { sink -> adapter.toJson(sink, value) }
}

inline fun <reified T> File.readSet(): Set<T> {
  source().buffer().use { source ->
    if (source.exhausted()) return emptySet()
    val setType = Types.newParameterizedType(Set::class.java, T::class.java)
    val adapter = DataConfig.moshi.adapter<Set<T>>(setType)
    return adapter.fromJson(source) ?: emptySet()
  }
}

inline fun <reified T> File.writeSet(values: Set<T>) {
  val setType = Types.newParameterizedType(Set::class.java, T::class.java)
  val adapter = DataConfig.moshi.adapter<Set<T>>(setType)
  sink().buffer().use { sink -> adapter.toJson(sink, values) }
}

/**
 * Explicitly perform disk operations that would normally violate [StrictMode].
 */
inline fun allowMainThreadDiskOperations(block: () -> Unit) {
  val diskReadPolicy = StrictMode.allowThreadDiskReads()
  val diskWritePolicy = StrictMode.allowThreadDiskWrites()
  block()
  StrictMode.setThreadPolicy(diskReadPolicy)
  StrictMode.setThreadPolicy(diskWritePolicy)
}
