package codes.chrishorner.socketweather.home

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.CurrentInformation
import codes.chrishorner.socketweather.data.CurrentObservations
import codes.chrishorner.socketweather.data.DateForecast
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.Loading
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.LocationFailed
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.NetworkFailed
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.Success
import codes.chrishorner.socketweather.home.HomeViewModel.State
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import java.text.DecimalFormat

class HomePresenter(view: View) {

  private val toolbar: Toolbar = view.findViewById(R.id.home_toolbar)
  private val locationDropdown: TextView = view.findViewById(R.id.home_locationDropdown)
  private val switchLocationContainer: View = view.findViewById(R.id.home_switchLocationContainer)
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

  init {
    toolbar.updatePaddingWithInsets(left = true, top = true, right = true)
    switchLocationContainer.updatePaddingWithInsets(left = true, top = true, right = true, bottom = true)
    val scroller: View = view.findViewById(R.id.home_scroller)
    scroller.updatePaddingWithInsets(bottom = true)

    locationDropdown.setOnClickListener { switchLocationContainer.isVisible = !switchLocationContainer.isVisible }
  }

  fun display(state: State) {
    locationDropdown.text = when (state.currentSelection) {
      is LocationSelection.Static -> state.currentSelection.location.name
      is LocationSelection.FollowMe -> state.currentLocation?.name ?: res.getString(R.string.home_findingLocation)
      is LocationSelection.None -> throw IllegalArgumentException("Cannot display LocationSelection.None")
    }

    if (state.forecasts != null && (state.loadingStatus == Loading || state.loadingStatus == Success)) {
      val observations: CurrentObservations = state.forecasts.observations
      val info: CurrentInformation = state.forecasts.info
      val dateForecasts: List<DateForecast> = state.forecasts.dateForecasts
      loading.isVisible = false
      forecastContainer.isVisible = true
      currentTemp.text = res.getString(R.string.temperatureFormat, observations.temp.format())
      feelsLikeTemp.text = res.getString(R.string.temperatureFormat, observations.temp_feels_like.format())

      highTemp.text = res.getString(R.string.temperatureFormat, dateForecasts[0].temp_max.format())
      val lowTempDegrees = dateForecasts[0].temp_min ?: if (info.is_night) info.temp_now else info.temp_later
      lowTemp.text = res.getString(R.string.temperatureFormat, lowTempDegrees.format())
    }

    error.isVisible = state.loadingStatus == LocationFailed || state.loadingStatus == NetworkFailed
    if (state.loadingStatus == LocationFailed) {
      errorMessage.setText(R.string.home_locationError)
    } else if (state.loadingStatus == NetworkFailed) {
      errorMessage.setText(R.string.home_networkError)
    }

    loading.isVisible = state.loadingStatus == Loading && state.forecasts == null
    secondaryLoading.isVisible = state.loadingStatus == Loading && state.forecasts != null
  }

  fun handleBack(): Boolean {
    if (switchLocationContainer.isVisible) {
      switchLocationContainer.isVisible = false
      return true
    }

    return false
  }

  private fun Float.format(): String = decimalFormat.format(this)
  private fun Int.format(): String = decimalFormat.format(this)
}
