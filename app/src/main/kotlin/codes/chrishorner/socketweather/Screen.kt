package codes.chrishorner.socketweather

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import codes.chrishorner.socketweather.util.inflate

enum class Screen(@LayoutRes private val layoutRes: Int) {

  Home(R.layout.home) {
    override fun getController(view: View): Controller = HomeController(view)
  },
  ChooseLocation(R.layout.choose_location) {
    override fun getController(view: View): Controller = ChooseLocationController()
  },
  About(R.layout.about) {
    override fun getController(view: View): Controller = AboutController()
  };

  private var controller: Controller? = null

  fun getView(parent: ViewGroup): View = parent.inflate(layoutRes)

  abstract fun getController(view: View): Controller

  fun bind(view: View) {
    controller = getController(view)
  }

  fun unbind(view: View) {
    controller?.onDestroy(view)
    controller = null
  }

  interface Controller {
    fun onDestroy(view: View) {}
  }
}
