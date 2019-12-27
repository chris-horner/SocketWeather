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
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State.Searching
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.android.widget.textChanges
import reactivecircus.flowbinding.appcompat.navigationClicks

class ChooseLocationPresenter(private val view: View) {

  private val toolbar: Toolbar = view.findViewById(R.id.chooseLocation_toolbar)
  private val followMeButton: View = view.findViewById(R.id.chooseLocation_followMeButton)
  private val title: View = view.findViewById(R.id.chooseLocation_title)

  val events: Flow<Event>

  init {
    view.updatePaddingWithInsets(top = true)

    val inputView: EditText = view.findViewById(R.id.chooseLocation_searchInput)

    events = merge(
        toolbar.navigationClicks().map { CloseClicked },
        followMeButton.clicks().map { FollowMeClicked },
        inputView.textChanges().map { InputSearch(it.toString()) }
    )
  }

  fun display(state: State) {
    when (state) {
      is Idle -> {
        title.isVisible = true
        toolbar.isVisible = !state.rootScreen
        followMeButton.isVisible = state.showFollowMe
      }

      is Searching -> {
        title.isVisible = false
        followMeButton.isVisible = false
      }
    }
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
