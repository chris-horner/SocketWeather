package codes.chrishorner.socketweather.choose_location

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.Error
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitted
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitting
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ClearInput
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.FollowMeClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ResultSelected
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.LocationSelectionStore
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.data.WeatherApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class ChooseLocationViewModel(
  showCloseButton: Boolean,
  private val api: WeatherApi,
  private val locationSelectionStore: LocationSelectionStore,
  overrideScope: CoroutineScope? = null
) : ViewModel() {

  private val scope = overrideScope ?: viewModelScope
  private val idleState = ChooseLocationState(showCloseButton, showFollowMe = !locationSelectionStore.hasFollowMeSaved)
  private val stateFlow = MutableStateFlow(idleState)
  private val searchQueryFlow = MutableStateFlow("")

  val states: StateFlow<ChooseLocationState> = stateFlow

  init {
    searchQueryFlow
      .map { query ->
        stateFlow.value.copy(query = query, loadingStatus = if (query.isBlank()) Idle else Searching)
      }
      .transformLatest { state ->
        emit(state)
        if (state.loadingStatus == Searching && state.query.length > 2) {
          delay(300)
          emit(search(state.query))
        }
      }
      .distinctUntilChanged()
      .onEach { stateFlow.value = it }
      .launchIn(scope)
  }

  fun handle(uiEvent: ChooseLocationUiEvent) = when (uiEvent) {
    is InputSearch -> searchQueryFlow.value = uiEvent.query
    is ResultSelected -> selectResult(uiEvent.result)
    ClearInput -> searchQueryFlow.value = ""
    is FollowMeClicked -> selectFollowMe(uiEvent.hasLocationPermission)
    CloseClicked -> { /* Not handled by ViewModel. */ }
  }

  private fun selectResult(result: SearchResult) {
    scope.launch {
      stateFlow.value = stateFlow.value.copy(loadingStatus = Submitting)

      try {
        val location = api.getLocation(result.geohash)
        locationSelectionStore.saveAndSelect(LocationSelection.Static(location))
        stateFlow.value = stateFlow.value.copy(loadingStatus = Submitted)
      } catch (e: Exception) {
        Timber.e(e, "Failed to select location.")
        stateFlow.value = stateFlow.value.copy(loadingStatus = SearchingDone, error = Error.Submission)
        delay(1_500L)
        stateFlow.value = stateFlow.value.copy(error = null)
      }
    }
  }

  private fun selectFollowMe(locationPermissionGranted: Boolean) {
    scope.launch {
      if (locationPermissionGranted) {
        locationSelectionStore.saveAndSelect(LocationSelection.FollowMe)
        stateFlow.value = stateFlow.value.copy(loadingStatus = Submitted)
      } else {
        stateFlow.value = stateFlow.value.copy(error = Error.Permission)
        delay(1_500L)
        stateFlow.value = stateFlow.value.copy(error = null)
      }
    }
  }

  private suspend fun search(query: String): ChooseLocationState {
    return try {
      val results = api.searchForLocation(query)
      stateFlow.value.copy(results = results, loadingStatus = SearchingDone)
    } catch (e: Exception) {
      Timber.e(e, "Search failed with query %s", query)
      stateFlow.value.copy(loadingStatus = SearchingError)
    }
  }

  private val LocationSelectionStore.hasFollowMeSaved
    get() = savedSelections.value.contains(LocationSelection.FollowMe)

  companion object {
    operator fun invoke(context: Context, showCloseButton: Boolean) = ChooseLocationViewModel(
      showCloseButton = showCloseButton,
      api = context.appSingletons.networkComponents.api,
      locationSelectionStore = context.appSingletons.locationSelectionStore
    )
  }
}
