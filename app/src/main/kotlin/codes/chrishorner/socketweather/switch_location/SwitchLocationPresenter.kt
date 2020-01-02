package codes.chrishorner.socketweather.switch_location

import android.view.View
import codes.chrishorner.socketweather.util.updatePaddingWithInsets

class SwitchLocationPresenter(view: View) {

  init {
    view.updatePaddingWithInsets(left = true, top = true, right = true, bottom = true)
  }
}
