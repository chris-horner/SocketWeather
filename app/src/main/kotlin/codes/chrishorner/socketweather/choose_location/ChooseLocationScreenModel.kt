package codes.chrishorner.socketweather.choose_location

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ClearInput
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.FollowMeClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationUiEvent.ResultSelected
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.Store
import codes.chrishorner.socketweather.data.WeatherApi
import codes.chrishorner.socketweather.util.CollectEffect
import codes.chrishorner.socketweather.util.MoleculeScreenModel
import codes.chrishorner.socketweather.util.updateValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class ChooseLocationScreenModel(
  showCloseButton: Boolean,
  private val api: WeatherApi,
  private val currentSelection: Store<LocationSelection>,
  private val savedSelections: Store<Set<LocationSelection>>,
) : MoleculeScreenModel<ChooseLocationUiEvent, ChooseLocationState> {

  private val initialState = ChooseLocationState(
    showCloseButton = showCloseButton,
    showFollowMe = !savedSelections.data.value.contains(LocationSelection.FollowMe),
  )

  @Composable
  override fun states(events: Flow<ChooseLocationUiEvent>): ChooseLocationState {
    val state = remember { mutableStateOf(initialState) }
    var query by remember { mutableStateOf("") }

    CollectEffect(events) { event ->
      when (event) {
        is InputSearch -> query = event.query
        ClearInput -> query = ""
        is FollowMeClicked -> TODO()
        is ResultSelected -> TODO()
        CloseClicked -> { /* Not handled by ScreenModel. */
        }
      }
    }

    LaunchedEffect(query) { search(query, state) }

    return state.value
  }

  private suspend fun search(query: String, state: MutableState<ChooseLocationState>) {

    val loadingStatus = if (query.isBlank()) Idle else Searching
    state.updateValue { it.copy(query = query, loadingStatus = loadingStatus) }

    if (query.length <= 2) return

    delay(300)
    val results = try {
      api.searchForLocation(query)
    } catch (e: Exception) {
      Timber.e(e, "Search failed with query %s", query)
      null
    }

    if (results == null) {
      state.updateValue { it.copy(loadingStatus = SearchingError) }
    } else {
      state.updateValue { it.copy(loadingStatus = SearchingDone, results = results) }
    }
  }

  companion object {
    operator fun invoke(context: Context, showCloseButton: Boolean) = ChooseLocationScreenModel(
      showCloseButton = showCloseButton,
      api = context.appSingletons.networkComponents.api,
      currentSelection = context.appSingletons.stores.currentSelection,
      savedSelections = context.appSingletons.stores.savedSelections
    )
  }
}
