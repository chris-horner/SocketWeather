package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context
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
  app: Application,
  moshi: Moshi
) : LocationSelectionStore {

  private val directory = app.getDir("location_choices", Context.MODE_PRIVATE)
  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  private val currentSelectionStore = DataStoreFactory.create(
    MoshiSerializer<LocationSelection>(moshi, default = LocationSelection.None)
  ) {
    getOrCreateFile(directory, "current_selection")
  }

  private val selectionsStore = DataStoreFactory.create(
    MoshiSerializer(moshi, default = emptySet<LocationSelection>())
  ) {
    getOrCreateFile(directory, "saved_selections")
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
