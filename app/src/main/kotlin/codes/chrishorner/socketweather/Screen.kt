package codes.chrishorner.socketweather

import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import codes.chrishorner.socketweather.util.inflate

enum class Screen(@LayoutRes private val layoutRes: Int) {
  Home(R.layout.home),
  ChooseLocation(R.layout.choose_location),
  About(R.layout.about);

  fun getView(parent: ViewGroup): View = parent.inflate(layoutRes)
}
