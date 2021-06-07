package codes.chrishorner.socketweather.data

import kotlinx.coroutines.flow.MutableStateFlow

class FakeLocationSelectionStore : LocationSelectionStore {

  private val currentSelectionState = MutableStateFlow<LocationSelection>(LocationSelection.None)
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
    currentSelectionState.value = LocationSelection.None
  }
}
