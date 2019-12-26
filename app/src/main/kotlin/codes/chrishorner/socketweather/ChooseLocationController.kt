package codes.chrishorner.socketweather

import android.view.View
import android.widget.EditText
import codes.chrishorner.socketweather.Screen.Controller
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.data.WeatherApi
import codes.chrishorner.socketweather.data.networkComponents
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import reactivecircus.flowbinding.android.widget.textChanges

class ChooseLocationController(view: View) : Controller {

  private val scope = MainScope()

  init {

    val api: WeatherApi = view.context.networkComponents().api
    val searchView: EditText = view.findViewById(R.id.chooseLocation_searchInput)

    searchView.textChanges()
        .debounce(400)
        .onStart { emit(searchView.text) }
        .filter { it.isNotBlank() }
        .mapLatest { query -> api.searchForLocation(query.trim().toString()) }
        .onEach { }
        .launchIn(scope)

    view.updatePaddingWithInsets(top = true)
  }

  override fun onDestroy(view: View) {
    scope.cancel()
  }

  private sealed class ChooseLocationUiState {

    data class Idle(
        val showFollowMe: Boolean,
        val rootScreen: Boolean
    ) : ChooseLocationUiState()

    data class Searching(
        val results: List<SearchResult>,
        val loading: Boolean,
        val error: Boolean,
        val rootScreen: Boolean
    ) : ChooseLocationUiState()

    object AttemptingToAdd : ChooseLocationUiState()
  }
}
