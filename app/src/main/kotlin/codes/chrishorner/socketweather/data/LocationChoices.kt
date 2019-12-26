package codes.chrishorner.socketweather.data

import androidx.annotation.MainThread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

@MainThread
object LocationChoices {

  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val savedSelectionsChannel = ConflatedBroadcastChannel<Set<LocationSelection>>()
  private val currentSelectionChannel = ConflatedBroadcastChannel<LocationSelection>()

  init {
    scope.launch {
      val savedLocations: Set<LocationSelection> = getFileForSavedSelections().readSet()
      savedSelectionsChannel.offer(savedLocations)
    }

    scope.launch {
      val locationSelection: LocationSelection? = getFileForCurrentSelection().readValue()
      currentSelectionChannel.offer(locationSelection ?: LocationSelection.None)
    }
  }

  fun observeSavedSelections(): Flow<Set<LocationSelection>> = savedSelectionsChannel.asFlow()

  fun observeCurrentSelection(): Flow<LocationSelection> = currentSelectionChannel.asFlow()

  fun saveAndSelect(selection: LocationSelection) {
    currentSelectionChannel.offer(selection)

    scope.launch {
      val file = getFileForSavedSelections()
      file.writeSet(file.readSet<LocationSelection>() + selection)
    }
  }

  fun select(selection: LocationSelection) {
    currentSelectionChannel.offer(selection)
    scope.launch { getFileForCurrentSelection().writeValue(selection) }
  }

  fun clear() {
    getFileForCurrentSelection().delete()
    getFileForSavedSelections().delete()
  }
}
