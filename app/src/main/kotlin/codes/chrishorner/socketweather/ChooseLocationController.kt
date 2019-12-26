package codes.chrishorner.socketweather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.os.bundleOf
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.data.WeatherApi
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import reactivecircus.flowbinding.android.widget.textChanges

class ChooseLocationController(args: Bundle) : ScopedController(args) {

  constructor(showCloseButton: Boolean) : this(bundleOf("showCloseButton" to showCloseButton))

  private val scope = MainScope()
  private val statesChannel = ConflatedBroadcastChannel<State>()
  private val api: WeatherApi = NetworkComponents.get().api

  init {
    val showCloseButton: Boolean = args.getBoolean("showCloseButton")
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(R.layout.choose_location, container, false)
  }

  override fun onAttach(view: View, viewScope: CoroutineScope) {
    view.updatePaddingWithInsets(top = true)
    val searchView: EditText = view.findViewById(R.id.chooseLocation_searchInput)

    searchView.textChanges()
        .debounce(400)
        .onStart { emit(searchView.text) }
        .filter { it.isNotBlank() }
        .mapLatest { query -> api.searchForLocation(query.trim().toString()) }
        .onEach { }
        .launchIn(viewScope)
  }

  override fun onDestroy() {
    scope.cancel()
  }

  private sealed class State {

    data class Idle(
        val showFollowMe: Boolean,
        val rootScreen: Boolean
    ) : State()

    data class Searching(
        val results: List<SearchResult>,
        val loading: Boolean,
        val error: Boolean,
        val rootScreen: Boolean
    ) : State()

    object AttemptingToAdd : State()
  }
}
