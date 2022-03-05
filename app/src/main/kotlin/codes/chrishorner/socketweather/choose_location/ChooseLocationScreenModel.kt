package codes.chrishorner.socketweather.choose_location

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.Error.Permission
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.Error.Submission
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
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.data.Store
import codes.chrishorner.socketweather.data.WeatherApi
import codes.chrishorner.socketweather.data.update
import codes.chrishorner.socketweather.util.CollectEffect
import codes.chrishorner.socketweather.util.MoleculeScreenModel
import codes.chrishorner.socketweather.util.Navigator
import codes.chrishorner.socketweather.util.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber

class ChooseLocationScreenModel(
  showCloseButton: Boolean,
  private val navigator: Navigator,
  private val api: WeatherApi,
  private val currentSelection: Store<LocationSelection>,
  private val savedSelections: Store<Set<LocationSelection>>,
) : MoleculeScreenModel<ChooseLocationUiEvent, ChooseLocationState>() {

  private val initialState = ChooseLocationState(
    showCloseButton = showCloseButton,
    showFollowMeButton = !savedSelections.data.value.contains(LocationSelection.FollowMe),
  )

  @Composable
  override fun states(events: Flow<ChooseLocationUiEvent>): ChooseLocationState {
    val scope = rememberCoroutineScope()
    val state = remember { mutableStateOf(initialState) }
    var query by remember { mutableStateOf("") }

    CollectEffect(events) { event ->
      when (event) {
        is InputSearch -> query = event.query
        ClearInput -> query = ""
        is FollowMeClicked -> scope.launch { selectFollowMe(event.hasLocationPermission, state) }
        is ResultSelected -> scope.launch { selectResult(event.result, state) }
        CloseClicked -> navigator.pop()
      }
    }

    LaunchedEffect(query) { search(query, state) }

    return state.value
  }

  private suspend fun search(query: String, state: MutableState<ChooseLocationState>) {

    val loadingStatus = if (query.isBlank()) Idle else Searching
    state.update { it.copy(query = query, loadingStatus = loadingStatus) }

    if (query.length <= 2) return

    delay(300)
    val results = try {
      api.searchForLocation(query)
    } catch (e: Exception) {
      Timber.e(e, "Search failed with query %s", query)
      null
    }

    if (results == null) {
      state.update { it.copy(loadingStatus = SearchingError) }
    } else {
      state.update { it.copy(loadingStatus = SearchingDone, results = results) }
    }
  }

  private suspend fun selectResult(result: SearchResult, state: MutableState<ChooseLocationState>) {
    state.update { it.copy(loadingStatus = Submitting) }

    try {
      val location = api.getLocation(result.geohash)
      val selection = LocationSelection.Static(location)
      savedSelections.update { it + selection }
      currentSelection.set(selection)
      state.update { it.copy(loadingStatus = Submitted) }
      // TODO: This should do more than pop.
      navigator.pop()
    } catch (e: Exception) {
      Timber.e(e, "Failed to select location.")
      state.update { it.copy(loadingStatus = SearchingDone, error = Submission) }
      delay(1_500L)
      state.update { it.copy(error = null) }
    }
  }

  private suspend fun selectFollowMe(locationPermissionGranted: Boolean, state: MutableState<ChooseLocationState>) {
    if (locationPermissionGranted) {
      savedSelections.update { it + LocationSelection.FollowMe }
      currentSelection.set(LocationSelection.FollowMe)
      state.update { it.copy(loadingStatus = Submitted) }
      // TODO: This should do more than pop.
      navigator.pop()
    } else {
      state.update { it.copy(loadingStatus = Idle, error = Permission) }
      delay(1_500L)
      state.update { it.copy(error = null) }
    }
  }

  companion object {
    operator fun invoke(showCloseButton: Boolean, navigator: Navigator, context: Context): ChooseLocationScreenModel {
      return ChooseLocationScreenModel(
        showCloseButton = showCloseButton,
        navigator = navigator,
        api = context.appSingletons.networkComponents.api,
        currentSelection = context.appSingletons.stores.currentSelection,
        savedSelections = context.appSingletons.stores.savedSelections
      )
    }
  }
}
