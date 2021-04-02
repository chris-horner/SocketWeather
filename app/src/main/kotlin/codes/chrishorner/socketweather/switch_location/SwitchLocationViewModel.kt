package codes.chrishorner.socketweather.switch_location

import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.LocationSelectionStore
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SwitchLocationViewModel(private val locationSelectionStore: LocationSelectionStore) {

  private val closeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  private val scope = MainScope()

  /**
   * Create a list where the current selection is at the beginning.
   */
  fun getOrderedSelections(): List<LocationSelection> {
    val selections: Set<LocationSelection> = locationSelectionStore.savedSelections.value
    val currentSelection: LocationSelection = locationSelectionStore.currentSelection.value
    return selections.sortedByDescending { it == currentSelection }
  }

  fun observeCloseEvents(): Flow<Unit> = closeEvents

  fun select(selection: LocationSelection) {
    scope.launch {
      locationSelectionStore.select(selection)
      closeEvents.emit(Unit)
    }
  }

  fun destroy() {
    scope.cancel()
  }
}
