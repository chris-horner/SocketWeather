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
  private val savedLocationsChannel = ConflatedBroadcastChannel<Set<Location>>()
  private val locationSelectionChannel = ConflatedBroadcastChannel<LocationSelection>()

  init {
    scope.launch {
      val savedLocations: Set<Location> = getFileForSavedLocations().readSet()
      savedLocationsChannel.offer(savedLocations)
    }

    scope.launch {
      val locationSelection: LocationSelection? = getFileForSelectedLocation().readValue()
      locationSelectionChannel.offer(locationSelection ?: LocationSelection.None)
    }
  }

  fun observeSavedLocations(): Flow<Set<Location>> = savedLocationsChannel.asFlow()

  fun observeLocationSelection(): Flow<LocationSelection> = locationSelectionChannel.asFlow()

  fun saveAndSelect(selection: LocationSelection) {
    locationSelectionChannel.offer(selection)

    if (selection is LocationSelection.Static) {
      scope.launch {
        val file = getFileForSavedLocations()
        file.writeSet(file.readSet<Location>() + selection.location)
      }
    }
  }

  fun select(selection: LocationSelection) {
    locationSelectionChannel.offer(selection)
    scope.launch { getFileForSelectedLocation().writeValue(selection) }
  }

  fun clear() {
    getFileForSelectedLocation().delete()
    getFileForSavedLocations().delete()
  }
}
