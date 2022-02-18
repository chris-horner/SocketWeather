package codes.chrishorner.socketweather.test

import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.LocationSelection.None
import codes.chrishorner.socketweather.data.LocationSelectionStore
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLocationSelectionStore : LocationSelectionStore {

  private val currentSelectionState = MutableStateFlow<LocationSelection>(None)
  private val savedSelectionState = MutableStateFlow(emptySet<LocationSelection>())

  override val currentSelection = currentSelectionState
  override val savedSelections = savedSelectionState

  override fun saveAndSelect(selection: LocationSelection) {
    savedSelectionState.value += selection
    currentSelectionState.value = selection
  }

  override fun select(selection: LocationSelection) {
    currentSelectionState.value = selection
  }

  override suspend fun clear() {
    savedSelectionState.value = emptySet()
    currentSelectionState.value = None
  }
}
