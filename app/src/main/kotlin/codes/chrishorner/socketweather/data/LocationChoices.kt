package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context
import codes.chrishorner.socketweather.util.getOrCreateFile
import codes.chrishorner.socketweather.util.readSet
import codes.chrishorner.socketweather.util.readValue
import codes.chrishorner.socketweather.util.writeSet
import codes.chrishorner.socketweather.util.writeValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LocationChoices(app: Application) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val savedSelectionsFlow: MutableStateFlow<Set<LocationSelection>>
  private val currentSelectionFlow: MutableStateFlow<LocationSelection>
  private val savedSelectionsFile: File
  private val currentSelectionFile: File

  init {
    val directory = app.getDir("location_choices", Context.MODE_PRIVATE)
    savedSelectionsFile = getOrCreateFile(directory, "saved_selections")
    currentSelectionFile = getOrCreateFile(directory, "current_selection")

    val savedLocations: Set<LocationSelection> = savedSelectionsFile.readSet()
    savedSelectionsFlow = MutableStateFlow(savedLocations)

    val locationSelection: LocationSelection = currentSelectionFile.readValue() ?: LocationSelection.None
    currentSelectionFlow = MutableStateFlow(locationSelection)
  }

  val currentSelection: LocationSelection
    get() = currentSelectionFlow.value

  val savedSelections: Set<LocationSelection>
    get() = savedSelectionsFlow.value

  val hasFollowMeSaved: Boolean
    get() = savedSelections.contains(LocationSelection.FollowMe)

  fun observeCurrentSelection(): Flow<LocationSelection> = currentSelectionFlow

  fun saveAndSelect(selection: LocationSelection) {
    scope.launch(Dispatchers.IO) {
      val selections = savedSelectionsFile.readSet<LocationSelection>() + selection
      savedSelectionsFile.writeSet(selections)
      currentSelectionFile.writeValue(selection)

      withContext(Dispatchers.Main) {
        savedSelectionsFlow.value = selections
        currentSelectionFlow.value = selection
      }
    }
  }

  fun select(selection: LocationSelection) {
    scope.launch {
      withContext(Dispatchers.IO) { currentSelectionFile.writeValue(selection) }
      currentSelectionFlow.value = selection
    }
  }

  fun clear() {
    savedSelectionsFile.delete()
    currentSelectionFile.delete()
  }
}
