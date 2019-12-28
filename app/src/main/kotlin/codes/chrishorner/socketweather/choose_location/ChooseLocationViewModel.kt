package codes.chrishorner.socketweather.choose_location

import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionSuccess
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.LoadingStatus.Submitting
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.data.WeatherApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseLocationViewModel(
    displayAsRoot: Boolean,
    showFollowMe: Boolean,
    private val api: WeatherApi,
    private val locationChoices: LocationChoices
) {

  private val scope = MainScope()
  private val idleState = State(displayAsRoot, showFollowMe)
  private val statesChannel = ConflatedBroadcastChannel(idleState)
  private val searchQueryChannel = ConflatedBroadcastChannel<String>()
  private val eventsChannel = BroadcastChannel<Event>(1)

  init {
    searchQueryChannel.asFlow()
        .debounce(200)
        .flatMapLatest { query ->
          if (query.isBlank()) {
            flow { emit(idleState) }
          } else {
            searchForResults(query)
          }
        }
        .onEach { statesChannel.offer(it) }
        .launchIn(scope)
  }

  fun observeStates(): Flow<State> = statesChannel.asFlow()

  fun observeEvents(): Flow<Event> = eventsChannel.asFlow()

  fun inputSearchQuery(query: String) {
    searchQueryChannel.offer(query)
  }

  fun selectResult(result: SearchResult) {
    scope.launch {
      statesChannel.offer(idleState.copy(loadingStatus = Submitting))

      try {
        val location = api.getLocation(result.geohash)
        locationChoices.saveAndSelect(LocationSelection.Static(location))
        eventsChannel.offer(SubmissionSuccess)
      } catch (e: Exception) {
        eventsChannel.offer(SubmissionError)
      }

      statesChannel.offer(idleState)
    }
  }

  fun selectFollowMe() {
    // TODO: Implement method.
  }

  fun destroy() {
    scope.cancel()
  }

  private fun searchForResults(query: String): Flow<State> = flow {
    emit(idleState.copy(loadingStatus = Searching))

    try {
      val results = withContext(Dispatchers.IO) { api.searchForLocation(query) }
      emit(idleState.copy(results = results, loadingStatus = SearchingDone))
    } catch (e: Exception) {
      emit(idleState.copy(loadingStatus = SearchingError))
    }
  }

  data class State(
      val rootScreen: Boolean,
      val showFollowMe: Boolean,
      val results: List<SearchResult> = emptyList(),
      val loadingStatus: LoadingStatus = Idle
  ) {
    enum class LoadingStatus { Idle, Searching, SearchingError, SearchingDone, Submitting }
  }

  enum class Event { SubmissionError, SubmissionSuccess }
}
