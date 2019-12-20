package codes.chrishorner.socketweather

import android.view.View
import codes.chrishorner.socketweather.Screen.Controller
import codes.chrishorner.socketweather.util.updatePaddingWithInsets

class HomeController(view: View) : Controller {

  init {
    val toolbar: View = view.findViewById(R.id.home_toolbar)
    toolbar.updatePaddingWithInsets(top = true)
  }
}
