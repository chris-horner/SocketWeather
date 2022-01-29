package codes.chrishorner.socketweather.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import codes.chrishorner.socketweather.util.getOrCreateFile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

interface AppStores {
  val forecast: Store<Forecast?>
  val savedSelections: Store<Set<LocationSelection>>
  val currentSelection: Store<LocationSelection>
  val location: Store<Location?>
}

class AppDiskStores(
  context: Context,
  private val moshi: Moshi,
) : AppStores {

  private val directory = context.filesDir

  override val forecast: Store<Forecast?> = blockingCreateStore("forecast", null)
  override val savedSelections: Store<Set<LocationSelection>> = blockingCreateStore("current_selection", emptySet())
  override val currentSelection: Store<LocationSelection> =
    blockingCreateStore("saved_selections", LocationSelection.None)
  override val location: Store<Location?> = blockingCreateStore("current_location", null)

  private inline fun <reified T> blockingCreateStore(fileName: String, default: T): Store<T> {
    val dataStore = DataStoreFactory.create(MoshiSerializer(moshi, default)) {
      getOrCreateFile(directory, fileName)
    }
    val values = runBlocking { dataStore.data.stateIn(this) }
    return DiskStore(default, dataStore, values)
  }

  private class DiskStore<T>(
    private val default: T,
    private val dataStore: DataStore<T>,
    override val data: StateFlow<T>
  ) : Store<T> {

    override suspend fun set(value: T) {
      dataStore.updateData { value }
    }

    override suspend fun clear() {
      dataStore.updateData { default }
    }
  }
}
