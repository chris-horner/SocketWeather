package codes.chrishorner.socketweather.home

import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.inflate
import kotlinx.coroutines.CoroutineScope

class HomeController : ScopedController<HomePresenter>() {

  override fun onCreateView(container: ViewGroup): View = container.inflate(R.layout.home)

  override fun onCreatePresenter(view: View): HomePresenter = HomePresenter(view)

  override fun onAttach(view: View, presenter: HomePresenter, viewScope: CoroutineScope) {
    // TODO: Render state into presenter.
  }

  override fun handleBack(): Boolean {
    if (getPresenter()?.handleBack() == true) return true
    return super.handleBack()
  }
}
