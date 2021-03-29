package codes.chrishorner.socketweather.data

import android.app.Application
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface LocationSelectionStore {
  val currentSelection: Flow<LocationSelection>
  val savedSelections: Flow<Set<LocationSelection>>
  fun saveAndSelect(selection: LocationSelection)
  fun select(selection: LocationSelection)
}

class LocationSelectionDataStore(
  private val app: Application,
  moshi: Moshi
) : LocationSelectionStore {

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  private val currentSelectionStore = DataStoreFactory.create(
    MoshiSerializer<LocationSelection>(moshi, default = LocationSelection.None)
  ) {
    app.dataStoreFile("currentSelection")
  }

  private val savedSelectionsStore = DataStoreFactory.create(
    MoshiSerializer(moshi, default = emptySet<LocationSelection>())
  ) {
    app.dataStoreFile("savedSelections")
  }

  override val currentSelection: Flow<LocationSelection> get() = currentSelectionStore.data

  override val savedSelections: Flow<Set<LocationSelection>> get() = savedSelectionsStore.data

  override fun saveAndSelect(selection: LocationSelection) {
    scope.launch {
      savedSelectionsStore.updateData { set -> set + selection }
      currentSelectionStore.updateData { selection }
    }
  }

  override fun select(selection: LocationSelection) {
    scope.launch {
      currentSelectionStore.updateData { selection }
    }
  }
}
