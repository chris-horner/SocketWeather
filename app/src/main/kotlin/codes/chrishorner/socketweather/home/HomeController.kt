package codes.chrishorner.socketweather.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.about.AboutController
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.getDeviceLocationUpdates
import codes.chrishorner.socketweather.home.HomePresenter.Event.AboutClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.RefreshClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.SwitchLocationClicked
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.asTransaction
import codes.chrishorner.socketweather.util.inflate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class HomeController : ScopedController<HomeViewModel, HomePresenter>() {

  override fun onCreateView(container: ViewGroup): View = container.inflate(R.layout.home)

  override fun onCreatePresenter(view: View): HomePresenter = HomePresenter(view)

  override fun onCreateViewModel(context: Context): HomeViewModel {
    return HomeViewModel(
        NetworkComponents.get().api,
        Clock.systemDefaultZone(),
        LocationChoices.get().observeCurrentSelection(),
        getDeviceLocationUpdates(context)
    )
  }

  override fun onAttach(view: View, presenter: HomePresenter, viewModel: HomeViewModel, viewScope: CoroutineScope) {
    viewModel.enableLocationUpdates(true)
    viewModel.observeStates().onEach { presenter.display(it) }.launchIn(viewScope)

    presenter.events
        .onEach { event ->
          when (event) {
            SwitchLocationClicked -> TODO("Display location switcher.")
            RefreshClicked -> viewModel.forceRefresh()
            AboutClicked -> router.pushController(AboutController().asTransaction())
          }
        }
        .launchIn(viewScope)

    // TODO: Move this logic into the presenter.
    // Update the refreshed time text every 10 seconds while the view is displayed.
    viewScope.launch {
      while (true) {
        delay(10_000)
        presenter.updateRefreshTimeText()
      }
    }

    // TODO: Move this logic into some kind of repository.
    // Refresh if we're displaying a forecast more than 1 minute old.
    viewModel.observeStates()
        .mapNotNull { it.forecast }
        .map { Duration.between(it.updateTime, Instant.now()) }
        .filter { it.toMinutes() > 1 }
        .onEach { viewModel.forceRefresh() }
        .launchIn(viewScope)
  }

  override fun onDetach(view: View, presenter: HomePresenter, viewModel: HomeViewModel, viewScope: CoroutineScope) {
    viewModel.enableLocationUpdates(false)
  }

  override fun onDestroy(viewModel: HomeViewModel?) {
    viewModel?.destroy()
  }
}
