package codes.chrishorner.socketweather.data

import android.app.Application
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

interface LocationSelectionStore {
  val currentSelection: StateFlow<LocationSelection>
  val savedSelections: StateFlow<Set<LocationSelection>>
  fun saveAndSelect(selection: LocationSelection)
  fun select(selection: LocationSelection)
  suspend fun clear()
}

/**
 * Initializing this class invokes a synchronous disk read.
 */
class LocationSelectionDiskStore(
  private val app: Application,
  moshi: Moshi
) : LocationSelectionStore {

  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  private val currentSelectionStore = DataStoreFactory.create(
    MoshiSerializer<LocationSelection>(moshi, default = LocationSelection.None)
  ) {
    app.dataStoreFile("current_selection")
  }

  private val selectionsStore = DataStoreFactory.create(
    MoshiSerializer(moshi, default = emptySet<LocationSelection>())
  ) {
    app.dataStoreFile("saved_selections")
  }

  override val currentSelection: StateFlow<LocationSelection>

  override val savedSelections: StateFlow<Set<LocationSelection>>

  init {
    val storedSelection = runBlocking { currentSelectionStore.data.first() }
    currentSelection = currentSelectionStore.data.stateIn(scope, SharingStarted.Eagerly, storedSelection)

    val storedSet = runBlocking { selectionsStore.data.first() }
    savedSelections = selectionsStore.data.stateIn(scope, SharingStarted.Eagerly, storedSet)
  }

  override fun saveAndSelect(selection: LocationSelection) {
    scope.launch {
      selectionsStore.updateData { set -> set + selection }
      currentSelectionStore.updateData { selection }
    }
  }

  override fun select(selection: LocationSelection) {
    scope.launch {
      currentSelectionStore.updateData { selection }
    }
  }

  override suspend fun clear() {
    currentSelectionStore.updateData { LocationSelection.None }
    selectionsStore.updateData { emptySet() }
  }
}
