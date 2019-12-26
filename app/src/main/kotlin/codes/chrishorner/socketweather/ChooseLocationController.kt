package codes.chrishorner.socketweather

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.data.WeatherApi
import codes.chrishorner.socketweather.data.networkComponents
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import com.bluelinelabs.conductor.Controller
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import reactivecircus.flowbinding.android.widget.textChanges

class ChooseLocationController : Controller() {

  private val scope = MainScope()

  override fun onAttach(view: View) {
    view.updatePaddingWithInsets(top = true)
    val api: WeatherApi = view.context.networkComponents().api
    val searchView: EditText = view.findViewById(R.id.chooseLocation_searchInput)

    searchView.textChanges()
        .debounce(400)
        .onStart { emit(searchView.text) }
        .filter { it.isNotBlank() }
        .mapLatest { query -> api.searchForLocation(query.trim().toString()) }
        .onEach { }
        .launchIn(scope)
  }

  override fun onDetach(view: View) {
    scope.cancel()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(R.layout.choose_location, container, false)
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
