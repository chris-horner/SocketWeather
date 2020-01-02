package codes.chrishorner.socketweather.switch_location

import android.content.Context
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.inflate

class SwitchLocationController : ScopedController<SwitchLocationViewModel, SwitchLocationPresenter>() {

  override fun onCreateViewModel(context: Context) = SwitchLocationViewModel()

  override fun onCreateView(container: ViewGroup): View = container.inflate(R.layout.switch_location)

  override fun onCreatePresenter(view: View): SwitchLocationPresenter {
    // TODO: Move this logic into a ViewModel.
    val locationChoices = LocationChoices.get()
    val selections: Set<LocationSelection> = locationChoices.getSavedSelections()
    val currentSelection: LocationSelection = locationChoices.getCurrentSelection()
    // Create a list where the current selection is at the beginning.
    //val orderedSelections: List<LocationSelection> = listOf(currentSelection) + (selections - currentSelection)
    val orderedSelections = selections.sortedBy { it == currentSelection }
    return SwitchLocationPresenter(view, orderedSelections)
  }
}
