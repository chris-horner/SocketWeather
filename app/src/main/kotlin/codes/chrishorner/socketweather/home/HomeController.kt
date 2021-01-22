package codes.chrishorner.socketweather.home

import android.content.Context
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.about.AboutChangeHandler
import codes.chrishorner.socketweather.about.AboutController
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.home.HomePresenter.Event.AboutClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.RefreshClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.SwitchLocationClicked
import codes.chrishorner.socketweather.switch_location.SwitchLocationChangeHandler
import codes.chrishorner.socketweather.switch_location.SwitchLocationController
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.asTransaction
import codes.chrishorner.socketweather.util.inflate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.threeten.bp.Clock

class HomeController : ScopedController<HomeViewModel, HomePresenter>() {

  override fun onCreateView(container: ViewGroup): View = container.inflate(R.layout.home)

  override fun onCreatePresenter(view: View, viewModel: HomeViewModel) = HomePresenter(view)

  override fun onCreateViewModel(context: Context): HomeViewModel =
      HomeViewModel(context.appSingletons.forecaster, Clock.systemDefaultZone())

  override fun onAttach(view: View, presenter: HomePresenter, viewModel: HomeViewModel, viewScope: CoroutineScope) {

    viewModel.states.onEach { presenter.display(it) }.launchIn(viewScope)
    viewModel.refreshIfNecessary()

    presenter.events
        .onEach { event ->
          when (event) {
            SwitchLocationClicked -> router.pushController(
                SwitchLocationController()
                    .asTransaction()
                    .pushChangeHandler(SwitchLocationChangeHandler())
            )
            RefreshClicked -> viewModel.forceRefresh()
            AboutClicked -> router.pushController(
                AboutController()
                    .asTransaction()
                    .pushChangeHandler(AboutChangeHandler())
                    .popChangeHandler(AboutChangeHandler())
            )
          }
        }
        .launchIn(viewScope)

    // Update the refreshed time text every 10 seconds while the view is displayed.
    viewScope.launch {
      while (true) {
        delay(10_000)
        presenter.updateRefreshTimeText()
      }
    }
  }
}
