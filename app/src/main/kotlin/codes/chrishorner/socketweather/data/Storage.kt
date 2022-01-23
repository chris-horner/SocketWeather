package codes.chrishorner.socketweather.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import codes.chrishorner.socketweather.util.getOrCreateFile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

interface Store<T> {
  val data: StateFlow<T>
  suspend fun set(value: T)
  suspend fun clear()
}

class DiskStore<T>(
  private val default: T,
  private val dataStore: DataStore<T>,
  override val data: StateFlow<T>
): Store<T> {

  override suspend fun set(value: T) {
    dataStore.updateData { value }
  }

  override suspend fun clear() {
    dataStore.updateData { default }
  }
}

class DiskStorage(
  val context: Context,
  val moshi: Moshi,
  val directory: String,
) {

  inline fun <reified T> blockingCreateStore(fileName: String, default: T): Store<T> {
    val folder = context.getDir(directory, Context.MODE_PRIVATE)
    val dataStore = DataStoreFactory.create(MoshiSerializer(moshi, default)) {
      getOrCreateFile(folder, fileName)
    }
    val values = runBlocking { dataStore.data.stateIn(this) }
    return DiskStore(default, dataStore, values)
  }
}
