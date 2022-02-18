package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import codes.chrishorner.socketweather.util.getOrCreateFile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

interface AppStores {
  val forecast: Store<Forecast?>
  val savedSelections: Store<Set<LocationSelection>>
  val currentSelection: Store<LocationSelection>
}

/**
 * Initialising this class invokes a synchronous disk read.
 */
class AppDiskStores(
  private val app: Application,
  private val moshi: Moshi,
) : AppStores {

  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  override val forecast: Store<Forecast?> = blockingCreateStore(
    fileName = "forecast",
    default = null,
  )
  override val savedSelections: Store<Set<LocationSelection>> = blockingCreateStore(
    fileName = "current_selection",
    default = emptySet(),
    overrideDir = "location_choices",
  )
  override val currentSelection: Store<LocationSelection> = blockingCreateStore(
    fileName = "saved_selections",
    default = LocationSelection.None,
    overrideDir = "location_choices",
  )

  /**
   * It would be simpler to keep all files in the same directory, but to maintain
   * backwards compatibility some files need to be placed in their own directories.
   */
  private inline fun <reified T> blockingCreateStore(
    fileName: String,
    default: T,
    overrideDir: String? = null
  ): Store<T> {
    val directory = if (overrideDir != null) {
      app.getDir(overrideDir, MODE_PRIVATE)
    } else {
      app.filesDir
    }

    val dataStore = DataStoreFactory.create(MoshiSerializer(moshi, default)) {
      getOrCreateFile(directory, fileName)
    }
    val initialValue = runBlocking { dataStore.data.first() }
    val values = dataStore.data.stateIn(scope, SharingStarted.Eagerly, initialValue)
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
