package codes.chrishorner.socketweather.home

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.util.updatePaddingWithInsets

class HomePresenter(view: View) {

  private val toolbar: Toolbar = view.findViewById(R.id.home_toolbar)
  private val locationDropdown: TextView = view.findViewById(R.id.home_locationDropdown)
  private val switchLocationContainer: View = view.findViewById(R.id.home_switchLocationContainer)

  init {
    toolbar.updatePaddingWithInsets(left = true, top = true, right = true)
    switchLocationContainer.updatePaddingWithInsets(left = true, top = true, right = true, bottom = true)
    locationDropdown.setOnClickListener { switchLocationContainer.isVisible = !switchLocationContainer.isVisible }
  }

  fun handleBack(): Boolean {
    if (switchLocationContainer.isVisible) {
      switchLocationContainer.isVisible = false
      return true
    }

    return false
  }
}
