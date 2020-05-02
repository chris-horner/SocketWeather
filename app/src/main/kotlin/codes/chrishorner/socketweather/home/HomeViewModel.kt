package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.data.Forecaster.State.Error
import codes.chrishorner.socketweather.data.Forecaster.State.ErrorType
import codes.chrishorner.socketweather.data.Forecaster.State.FindingLocation
import codes.chrishorner.socketweather.data.Forecaster.State.Loaded
import codes.chrishorner.socketweather.data.Forecaster.State.LoadingForecast
import codes.chrishorner.socketweather.data.Forecaster.State.Refreshing
import codes.chrishorner.socketweather.data.Location
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomeViewModel.State.LoadingStatus.Loading
import codes.chrishorner.socketweather.home.HomeViewModel.State.LoadingStatus.LocationFailed
import codes.chrishorner.socketweather.home.HomeViewModel.State.LoadingStatus.NetworkFailed
import codes.chrishorner.socketweather.home.HomeViewModel.State.LoadingStatus.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeViewModel(private val forecaster: Forecaster) {

  //TODO: Remove this mapping once HomePresenter has been cleaned up.
  fun observeStates(): Flow<State> = forecaster.observeState()
      .map { forecasterState ->
        when (forecasterState) {
          is FindingLocation -> State(
              selection = forecasterState.selection,
              location = null,
              forecast = null,
              loadingStatus = Loading
          )

          is LoadingForecast -> State(
              selection = forecasterState.selection,
              location = forecasterState.location,
              forecast = null,
              loadingStatus = Loading
          )

          is Loaded -> State(
              selection = forecasterState.selection,
              location = forecasterState.forecast.location,
              forecast = forecasterState.forecast,
              loadingStatus = Success
          )

          is Refreshing -> State(
              selection = forecasterState.selection,
              location = forecasterState.previousForecast.location,
              forecast = forecasterState.previousForecast,
              loadingStatus = Loading
          )

          is Error -> State(
              selection = forecasterState.selection,
              location = null,
              forecast = null,
              loadingStatus = if (forecasterState.type == ErrorType.LOCATION) LocationFailed else NetworkFailed
          )
        }
      }

  fun forceRefresh() {
    forecaster.refresh()
  }

  data class State(
      val selection: LocationSelection,
      val location: Location? = null,
      val forecast: Forecast? = null,
      val loadingStatus: LoadingStatus
  ) {
    enum class LoadingStatus { Loading, LocationFailed, NetworkFailed, Success }
  }
}
