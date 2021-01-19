package codes.chrishorner.socketweather.switch_location

import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.LocationSelection
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class SwitchLocationViewModel(private val locationChoices: LocationChoices) {

  private val closeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
  private val scope = MainScope()

  /**
   * Create a list where the current selection is at the beginning.
   */
  fun getOrderedSelections(): List<LocationSelection> {
    val selections: Set<LocationSelection> = locationChoices.savedSelections
    val currentSelection: LocationSelection = locationChoices.currentSelection
    return selections.sortedByDescending { it == currentSelection }
  }

  fun observeCloseEvents(): Flow<Unit> = closeEvents

  fun select(selection: LocationSelection) {
    scope.launch {
      locationChoices.select(selection)
      closeEvents.emit(Unit)
    }
  }

  fun destroy() {
    scope.cancel()
  }
}
