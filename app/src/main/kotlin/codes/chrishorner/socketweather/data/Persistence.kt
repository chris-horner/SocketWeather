package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations
import com.squareup.moshi.Types
import okio.buffer
import okio.sink
import okio.source
import java.io.File

private lateinit var directory: File

fun initialisePersistenceFiles(app: Application) {
  allowMainThreadDiskOperations {
    directory = app.getDir("persistence", Context.MODE_PRIVATE)
  }
}

fun getFileForSavedLocations(): File = File(directory, "saved_locations")

fun getFileForSelectedLocation(): File = File(directory, "selected_location")

fun getFileForCurrentForecast(): File = File(directory, "current_forecast")

inline fun <reified T> File.readValue(): T? {
  val adapter = DataConfig.moshi.adapter(T::class.java)
  return adapter.fromJson(source().buffer())
}

inline fun <reified T> File.writeValue(value: T) {
  val adapter = DataConfig.moshi.adapter(T::class.java)
  adapter.toJson(sink().buffer(), value)
}

inline fun <reified T> File.readSet(): Set<T> {
  val setType = Types.newParameterizedType(Set::class.java, T::class.java)
  val adapter = DataConfig.moshi.adapter<Set<T>>(setType)
  return adapter.fromJson(source().buffer()) ?: emptySet()
}

inline fun <reified T> File.writeSet(values: Set<T>) {
  val setType = Types.newParameterizedType(Set::class.java, T::class.java)
  val adapter = DataConfig.moshi.adapter<Set<T>>(setType)
  adapter.toJson(sink().buffer(), values)
}
