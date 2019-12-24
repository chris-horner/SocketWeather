package codes.chrishorner.socketweather

import android.view.View
import codes.chrishorner.socketweather.Screen.Controller
import codes.chrishorner.socketweather.util.updatePaddingWithInsets

class ChooseLocationController(view: View) : Controller {

  init {
    view.updatePaddingWithInsets(top = true)
  }
}
