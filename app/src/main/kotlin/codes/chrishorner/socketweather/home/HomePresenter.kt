package codes.chrishorner.socketweather.home

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomePresenter.Event.AboutClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.RefreshClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.SwitchLocationClicked
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.Loading
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.LocationFailed
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.NetworkFailed
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.Success
import codes.chrishorner.socketweather.home.HomeViewModel.State
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.appcompat.itemClicks
import java.text.DecimalFormat

class HomePresenter(view: View) {

  private val toolbar: Toolbar = view.findViewById(R.id.home_toolbar)
  private val locationDropdown: TextView = view.findViewById(R.id.home_locationDropdown)
  private val forecastContainer: View = view.findViewById(R.id.home_forecastContainer)
  private val loading: View = view.findViewById(R.id.home_loading)
  private val secondaryLoading: View = view.findViewById(R.id.home_secondaryLoading)
  private val error: View = view.findViewById(R.id.home_error)
  private val errorMessage: TextView = view.findViewById(R.id.home_errorMessage)
  private val currentTemp: TextView = view.findViewById(R.id.home_currentTemp)
  private val feelsLikeTemp: TextView = view.findViewById(R.id.home_feelsLikeTemp)
  private val highTemp: TextView = view.findViewById(R.id.home_highTemp)
  private val lowTemp: TextView = view.findViewById(R.id.home_lowTemp)

  private val res: Resources = view.resources
  private val decimalFormat = DecimalFormat("0.#")

  val events: Flow<Event>

  init {
    toolbar.updatePaddingWithInsets(left = true, top = true, right = true)
    val scroller: View = view.findViewById(R.id.home_scroller)
    scroller.updatePaddingWithInsets(bottom = true)

    val menuEvents: Flow<Event> = toolbar.itemClicks().map {
      when (it.itemId) {
        R.id.menu_refresh -> RefreshClicked
        R.id.menu_about -> AboutClicked
        else -> throw IllegalArgumentException("Unknown item selected.")
      }
    }

    events = merge(
        locationDropdown.clicks().map { SwitchLocationClicked },
        menuEvents
    )
  }

  fun display(state: State) {
    locationDropdown.text = when (state.currentSelection) {
      is LocationSelection.Static -> state.currentSelection.location.name
      is LocationSelection.FollowMe -> state.currentLocation?.name ?: res.getString(R.string.home_findingLocation)
      is LocationSelection.None -> throw IllegalArgumentException("Cannot display LocationSelection.None")
    }

    val forecast: Forecast? = state.forecast

    if (forecast != null && (state.loadingStatus == Loading || state.loadingStatus == Success)) {
      loading.isVisible = false
      forecastContainer.isVisible = true
      currentTemp.text = res.getString(R.string.temperatureFormat, forecast.currentTemp.format())
      feelsLikeTemp.text = res.getString(R.string.temperatureFormat, forecast.tempFeelsLike.format())
      highTemp.text = res.getString(R.string.temperatureFormat, forecast.highTemp.format())
      lowTemp.text = res.getString(R.string.temperatureFormat, forecast.lowTemp.format())
    }

    error.isVisible = state.loadingStatus == LocationFailed || state.loadingStatus == NetworkFailed
    if (state.loadingStatus == LocationFailed) {
      errorMessage.setText(R.string.home_locationError)
    } else if (state.loadingStatus == NetworkFailed) {
      errorMessage.setText(R.string.home_networkError)
    }

    loading.isVisible = state.loadingStatus == Loading && forecast == null
    secondaryLoading.isVisible = state.loadingStatus == Loading && forecast != null
  }

  enum class Event { SwitchLocationClicked, RefreshClicked, AboutClicked }

  private fun Float.format(): String = decimalFormat.format(this)
  private fun Int.format(): String = decimalFormat.format(this)
}
