package codes.chrishorner.socketweather.choose_location

import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.PermissionError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionSuccess
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.LoadingStatus.Submitting
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.data.WeatherApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ChooseLocationViewModel(
    displayAsRoot: Boolean,
    private val api: WeatherApi,
    private val locationChoices: LocationChoices
) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
  private val idleState = State(displayAsRoot, showFollowMe = !locationChoices.hasFollowMeSaved)
  private val statesFlow = MutableStateFlow(idleState)
  private val searchQueryFlow = MutableStateFlow("")
  private val events = MutableSharedFlow<Event>(extraBufferCapacity = 1)

  init {
    searchQueryFlow
        .map { query ->
          if (query.isBlank()) {
            query to idleState
          } else {
            query to idleState.copy(loadingStatus = Searching)
          }
        }
        .transformLatest { (query, state) ->
          emit(state)
          if (state.loadingStatus == Searching && query.length > 2) {
            delay(300)
            emit(search(query))
          }
        }
        .distinctUntilChanged()
        .onEach { statesFlow.value = it }
        .launchIn(scope)
  }

  fun observeStates(): Flow<State> = statesFlow

  fun observeEvents(): Flow<Event> = events

  fun inputSearchQuery(query: String) {
    searchQueryFlow.value = query
  }

  fun selectResult(result: SearchResult) {
    scope.launch {
      statesFlow.value = idleState.copy(loadingStatus = Submitting)

      try {
        val location = api.getLocation(result.geohash)
        locationChoices.saveAndSelect(LocationSelection.Static(location))
        events.emit(SubmissionSuccess)
      } catch (e: Exception) {
        Timber.e(e, "Failed to select location.")
        events.emit(SubmissionError)
      }

      statesFlow.value = idleState
    }
  }

  fun selectFollowMe(locationPermissionGranted: Boolean) {
    if (locationPermissionGranted) {
      scope.launch {
        locationChoices.saveAndSelect(LocationSelection.FollowMe)
        events.emit(SubmissionSuccess)
      }
    } else {
      events.tryEmit(PermissionError)
    }
  }

  fun destroy() {
    scope.cancel()
  }

  private suspend fun search(query: String): State {
    return try {
      val results = withContext(Dispatchers.IO) { api.searchForLocation(query) }
      idleState.copy(results = results, loadingStatus = SearchingDone)
    } catch (e: Exception) {
      Timber.e(e, "Search failed with query %s", query)
      idleState.copy(loadingStatus = SearchingError)
    }
  }

  data class State(
      val rootScreen: Boolean,
      val showFollowMe: Boolean,
      val results: List<SearchResult> = emptyList(),
      val loadingStatus: LoadingStatus = Idle
  )

  enum class LoadingStatus { Idle, Searching, SearchingError, SearchingDone, Submitting }

  enum class Event { SubmissionError, SubmissionSuccess, PermissionError }
}
