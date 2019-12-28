package codes.chrishorner.socketweather.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R.layout
import com.bluelinelabs.conductor.Controller

class HomeController : Controller() {

  private var presenter: HomePresenter? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(layout.home, container, false)
  }

  override fun onAttach(view: View) {
    presenter = HomePresenter(view)
    // TODO: Render state into presenter.
  }

  override fun handleBack(): Boolean {
    if (presenter?.handleBack() == true) return true
    return super.handleBack()
  }
}
