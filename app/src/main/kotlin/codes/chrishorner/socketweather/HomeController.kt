package codes.chrishorner.socketweather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller

class HomeController : Controller() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(R.layout.home, container, false)
  }
}
