package codes.chrishorner.socketweather.choose_location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.chrishorner.socketweather.choose_location.ChooseLocationDataEvent.PermissionError
import codes.chrishorner.socketweather.choose_location.ChooseLocationDataEvent.SubmissionError
import codes.chrishorner.socketweather.choose_location.ChooseLocationDataEvent.SubmissionSuccess
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitted
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitting
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.FollowMeClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ResultSelected
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.data.WeatherApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ChooseLocationViewModel(
    showCloseButton: Boolean,
    private val api: WeatherApi,
    private val locationChoices: LocationChoices
) : ViewModel() {

  private val idleState = ChooseLocationState(showCloseButton, showFollowMe = !locationChoices.hasFollowMeSaved)
  private val statesFlow = MutableStateFlow(idleState)
  private val searchQueryFlow = MutableStateFlow("")
  private val eventsFlow = MutableSharedFlow<ChooseLocationDataEvent>(extraBufferCapacity = 1)

  val states: StateFlow<ChooseLocationState> = statesFlow
  val events: Flow<ChooseLocationDataEvent> = eventsFlow

  init {
    searchQueryFlow
        .map { query ->
          idleState.copy(query = query, loadingStatus = if (query.isBlank()) Idle else Searching)
        }
        .transformLatest { state ->
          emit(state)
          if (state.loadingStatus == Searching && state.query.length > 2) {
            delay(300)
            emit(search(state.query))
          }
        }
        .distinctUntilChanged()
        .onEach { statesFlow.value = it }
        .launchIn(viewModelScope)
  }

  fun handle(uiEvent: ChooseLocationUiEvent) {
    when (uiEvent) {
      is InputSearch -> searchQueryFlow.value = uiEvent.query
      is ResultSelected -> selectResult(uiEvent.result)
      FollowMeClicked -> TODO()
      CloseClicked -> TODO()
    }
  }

  fun observeEvents(): Flow<ChooseLocationDataEvent> = events

  fun inputSearchQuery(query: String) {
    searchQueryFlow.value = query
  }

  fun selectResult(result: SearchResult) {
    viewModelScope.launch {
      statesFlow.value = statesFlow.value.copy(loadingStatus = Submitting)

      try {
        val location = api.getLocation(result.geohash)
        locationChoices.saveAndSelect(LocationSelection.Static(location))
        eventsFlow.emit(SubmissionSuccess)
        statesFlow.value = statesFlow.value.copy(loadingStatus = Submitted)
      } catch (e: Exception) {
        Timber.e(e, "Failed to select location.")
        eventsFlow.emit(SubmissionError)
        statesFlow.value = statesFlow.value.copy(loadingStatus = SearchingError)
      }
    }
  }

  fun selectFollowMe(locationPermissionGranted: Boolean) {
    if (locationPermissionGranted) {
      viewModelScope.launch {
        locationChoices.saveAndSelect(LocationSelection.FollowMe)
        eventsFlow.emit(SubmissionSuccess)
      }
    } else {
      eventsFlow.tryEmit(PermissionError)
    }
  }

  private suspend fun search(query: String): ChooseLocationState {
    return try {
      val results = withContext(Dispatchers.IO) { api.searchForLocation(query) }
      statesFlow.value.copy(results = results, loadingStatus = SearchingDone)
    } catch (e: Exception) {
      Timber.e(e, "Search failed with query %s", query)
      statesFlow.value.copy(loadingStatus = SearchingError)
    }
  }
}
