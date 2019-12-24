package codes.chrishorner.socketweather

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import codes.chrishorner.socketweather.util.inflate

enum class Screen(@LayoutRes private val layoutRes: Int) {

  Home(R.layout.home) {
    override fun getController(view: View, navigator: Navigator): Controller = HomeController(view)
  },
  ChooseLocation(R.layout.choose_location) {
    override fun getController(view: View, navigator: Navigator): Controller = ChooseLocationController(view)
  },
  About(R.layout.about) {
    override fun getController(view: View, navigator: Navigator): Controller = AboutController()
  };

  private var controller: Controller? = null

  fun getView(parent: ViewGroup): View = parent.inflate(layoutRes)

  protected abstract fun getController(view: View, navigator: Navigator): Controller

  fun bind(view: View, navigator: Navigator) {
    controller = getController(view, navigator)
  }

  fun unbind(view: View) {
    controller?.onDestroy(view)
    controller = null
  }

  interface Controller {
    fun onDestroy(view: View) {}
  }

  interface Navigator {
    fun goTo(screen: Screen)
    fun goBack()
  }
}
