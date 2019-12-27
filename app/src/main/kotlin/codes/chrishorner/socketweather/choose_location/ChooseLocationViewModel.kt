package codes.chrishorner.socketweather.choose_location

import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionSuccess
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.FailedSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.LoadedResults
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.Submitting
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.LocationSelection.FollowMe
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.data.WeatherApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseLocationViewModel(private val displayAsRoot: Boolean, private val api: WeatherApi) {

  private val scope = MainScope()
  private val statesChannel = ConflatedBroadcastChannel<State>()
  private val searchQueryChannel = ConflatedBroadcastChannel<String>()
  private val eventsChannel = BroadcastChannel<Event>(1)

  init {
    scope.launch {
      val currentSelections = LocationChoices.observeSavedSelections().first()
      val showFollowMe = currentSelections.none { it == FollowMe }

      searchQueryChannel.asFlow()
          .debounce(400)
          .flatMapLatest { query ->
            if (query.isBlank()) {
              flow { emit(Idle(displayAsRoot, showFollowMe)) }
            } else {
              searchForResults(query)
            }
          }
          .onStart { emit(Idle(displayAsRoot, showFollowMe)) }
          .collect {
            statesChannel.offer(it)
          }
    }
  }

  fun observeStates(): Flow<State> = statesChannel.asFlow()

  fun observeEvents(): Flow<Event> = eventsChannel.asFlow()

  fun inputSearchQuery(query: String) {
    searchQueryChannel.offer(query)
  }

  fun selectResult(result: SearchResult) {
    scope.launch {
      statesChannel.offer(Submitting)

      try {
        val location = api.getLocation(result.geohash)
        LocationChoices.saveAndSelect(LocationSelection.Static(location))
        eventsChannel.offer(SubmissionSuccess)
      } catch (e: Exception) {
        val currentSelections = LocationChoices.observeSavedSelections().first()
        val showFollowMe = currentSelections.none { it == FollowMe }
        statesChannel.offer(Idle(displayAsRoot, showFollowMe))
        eventsChannel.offer(SubmissionError)
      }
    }
  }

  fun selectFollowMe() {
    // TODO: Implement method.
  }

  fun destroy() {
    scope.cancel()
  }

  private fun searchForResults(query: String): Flow<State> = flow {
    emit(Searching(displayAsRoot, query))
    try {
      val results = withContext(Dispatchers.IO) { api.searchForLocation(query) }
      emit(LoadedResults(displayAsRoot, query, results))
    } catch (e: Exception) {
      emit(FailedSearch(displayAsRoot, query))
    }
  }

  sealed class State {

    data class Idle(
        val rootScreen: Boolean,
        val showFollowMe: Boolean
    ) : State()

    data class LoadedResults(
        val rootScreen: Boolean,
        val query: String,
        val results: List<SearchResult>
    ) : State()

    data class FailedSearch(
        val rootScreen: Boolean,
        val query: String
    ) : State()

    data class Searching(
        val rootScreen: Boolean,
        val query: String
    ) : State()

    object Submitting : State()
  }

  enum class Event { SubmissionError, SubmissionSuccess }
}
