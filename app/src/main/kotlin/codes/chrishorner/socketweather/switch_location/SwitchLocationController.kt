package codes.chrishorner.socketweather.switch_location

import android.content.Context
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.inflate

class SwitchLocationController : ScopedController<SwitchLocationViewModel, SwitchLocationPresenter>() {

  override fun onCreateViewModel(context: Context) = SwitchLocationViewModel()

  override fun onCreateView(container: ViewGroup): View = container.inflate(R.layout.switch_location)

  override fun onCreatePresenter(view: View): SwitchLocationPresenter {
    return SwitchLocationPresenter(view)
  }
}
