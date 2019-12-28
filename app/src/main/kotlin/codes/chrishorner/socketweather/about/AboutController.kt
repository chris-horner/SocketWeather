package codes.chrishorner.socketweather.about

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R.layout
import com.bluelinelabs.conductor.Controller

class AboutController : Controller() {

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(layout.about, container, false)
  }
}
