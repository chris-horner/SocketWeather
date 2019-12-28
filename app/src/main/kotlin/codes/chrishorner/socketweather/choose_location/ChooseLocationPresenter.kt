package codes.chrishorner.socketweather.choose_location

import android.view.View
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.FollowMeClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.android.widget.textChanges
import reactivecircus.flowbinding.appcompat.navigationClicks

class ChooseLocationPresenter(private val view: View) {

  private val toolbar: Toolbar = view.findViewById(R.id.chooseLocation_toolbar)
  private val followMeButton: View = view.findViewById(R.id.chooseLocation_followMeButton)
  private val topContainer: View = view.findViewById(R.id.chooseLocation_topContainer)

  val events: Flow<Event>

  init {
    view.updatePaddingWithInsets(left = true, top = true, right = true, bottom = true)

    val inputView: EditText = view.findViewById(R.id.chooseLocation_searchInput)
    val inputSearches: Flow<InputSearch> = inputView.textChanges()
        .onEach { topContainer.isVisible = it.isBlank() }
        .map { InputSearch(it.toString()) }

    events = merge(
        toolbar.navigationClicks().map { CloseClicked },
        followMeButton.clicks().map { FollowMeClicked },
        inputSearches
    )
  }

  fun display(state: State) {
    toolbar.isVisible = state.rootScreen
    followMeButton.isVisible = state.showFollowMe
  }

  fun showError() {
    Snackbar.make(view, R.string.chooseLocation_submissionError, Snackbar.LENGTH_SHORT).show()
  }

  sealed class Event {
    data class ResultSelected(val result: SearchResult) : Event()
    data class InputSearch(val query: String) : Event()
    object FollowMeClicked : Event()
    object CloseClicked : Event()
  }
}
