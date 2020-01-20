package codes.chrishorner.socketweather.switch_location

import android.content.Context
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.choose_location.ChooseLocationController
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.getLocationChoices
import codes.chrishorner.socketweather.switch_location.SwitchLocationPresenter.Event.AddLocationClicked
import codes.chrishorner.socketweather.switch_location.SwitchLocationPresenter.Event.DismissClicked
import codes.chrishorner.socketweather.switch_location.SwitchLocationPresenter.Event.LocationClicked
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.asTransaction
import codes.chrishorner.socketweather.util.inflate
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SwitchLocationController : ScopedController<SwitchLocationViewModel, SwitchLocationPresenter>() {

  override fun onCreateViewModel(context: Context) = SwitchLocationViewModel(context.getLocationChoices())

  override fun onCreateView(container: ViewGroup): View = container.inflate(R.layout.switch_location)

  override fun onCreatePresenter(view: View, viewModel: SwitchLocationViewModel): SwitchLocationPresenter {
    return SwitchLocationPresenter(view, viewModel.getOrderedSelections())
  }

  override fun onAttach(
      view: View,
      presenter: SwitchLocationPresenter,
      viewModel: SwitchLocationViewModel,
      viewScope: CoroutineScope
  ) {

    presenter.events
        .onEach { event ->
          when (event) {
            is LocationClicked -> viewModel.select(event.selection)
            DismissClicked -> router.popCurrentController()
            AddLocationClicked -> {
              // TODO: Remove displayFollowMe argument.
              navigateToChooseLocation(!view.context.getLocationChoices().hasFollowMeSaved())
            }
          }
        }
        .launchIn(viewScope)

    viewModel.observeCloseEvents()
        .onEach { router.popCurrentController() }
        .launchIn(viewScope)
  }

  private fun navigateToChooseLocation(displayFollowMe: Boolean) {
    val chooseController = ChooseLocationController(displayFollowMe, displayAsRoot = false)
    val backstack: MutableList<RouterTransaction> = router.backstack
    backstack.removeAt(backstack.size - 1)
    backstack.add(chooseController.asTransaction())
    router.setBackstack(backstack, null)
  }

  override fun onDestroy(viewModel: SwitchLocationViewModel?) {
    viewModel?.destroy()
  }
}
