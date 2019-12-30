package codes.chrishorner.socketweather.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.getDeviceLocationUpdates
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.inflate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeController : ScopedController<HomeViewModel, HomePresenter>() {

  override fun onCreateView(container: ViewGroup): View = container.inflate(R.layout.home)

  override fun onCreatePresenter(view: View): HomePresenter = HomePresenter(view)

  override fun onCreateViewModel(context: Context): HomeViewModel {
    return HomeViewModel(
        NetworkComponents.get().api,
        LocationChoices.get().observeSavedSelections(),
        LocationChoices.get().observeCurrentSelection(),
        getDeviceLocationUpdates(context)
    )
  }

  override fun onAttach(view: View, presenter: HomePresenter, viewModel: HomeViewModel, viewScope: CoroutineScope) {
    viewModel.enableLocationUpdates(true)
    viewModel.observeStates().onEach { presenter.display(it) }.launchIn(viewScope)
  }

  override fun onDetach(view: View, presenter: HomePresenter, viewModel: HomeViewModel, viewScope: CoroutineScope) {
    viewModel.enableLocationUpdates(false)
  }

  override fun handleBack(): Boolean {
    if (getPresenter()?.handleBack() == true) return true
    return super.handleBack()
  }

  override fun onDestroy(viewModel: HomeViewModel?) {
    viewModel?.destroy()
  }
}
