package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import codes.chrishorner.socketweather.util.getOrCreateFile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking

interface AppStores {
  val forecast: Store<Forecast?>
  val savedSelections: Store<Set<LocationSelection>>
  val currentSelection: Store<LocationSelection>
  val lastKnownLocation: Store<DeviceLocation?>
  suspend fun clear()
}

/**
 * Initialising this class invokes a synchronous disk read.
 */
class AppDiskStores(
  private val app: Application,
  private val moshi: Moshi,
) : AppStores {

  override val forecast: Store<Forecast?> = blockingCreateStore(
    fileName = "forecast",
    default = null,
  )
  override val savedSelections: Store<Set<LocationSelection>> = blockingCreateStore(
    fileName = "saved_selections",
    default = emptySet(),
    overrideDir = "location_choices",
  )
  override val currentSelection: Store<LocationSelection> = blockingCreateStore(
    fileName = "current_selection",
    default = LocationSelection.None,
    overrideDir = "location_choices",
  )
  override val lastKnownLocation: Store<DeviceLocation?> = blockingCreateStore(
    fileName = "last_known_location",
    default = null,
  )

  override suspend fun clear() {
    forecast.clear()
    savedSelections.clear()
    currentSelection.clear()
    lastKnownLocation.clear()
  }

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
    val initial = runBlocking { dataStore.data.first() }
    return DiskStore(default, initial, dataStore)
  }

  private class DiskStore<T>(
    private val default: T,
    initial: T,
    private val dataStore: DataStore<T>,
  ) : Store<T> {

    override val data = MutableStateFlow(initial)

    override suspend fun set(value: T) {
      dataStore.updateData {
        data.update { value }
        value
      }
    }

    override suspend fun clear() {
      dataStore.updateData {
        data.update { default }
        default
      }
    }
  }
}
