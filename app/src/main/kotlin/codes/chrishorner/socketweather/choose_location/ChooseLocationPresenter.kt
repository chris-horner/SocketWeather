package codes.chrishorner.socketweather.choose_location

import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.FollowMeClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.ResultSelected
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.State
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

  private val container = view.findViewById<ViewGroup>(R.id.chooseLocation_container)
  private val toolbar: Toolbar = view.findViewById(R.id.chooseLocation_toolbar)
  private val titleView: View = view.findViewById(R.id.chooseLocation_title)
  private val followMeButton: View = view.findViewById(R.id.chooseLocation_followMeButton)
  private val inputView: EditText = view.findViewById(R.id.chooseLocation_searchInput)
  private val recycler: RecyclerView = view.findViewById(R.id.chooseLocation_recycler)
  private val loadingView: View = view.findViewById(R.id.chooseLocation_loadingResults)
  private val errorView: View = view.findViewById(R.id.chooseLocation_error)
  private val emptyView: View = view.findViewById(R.id.chooseLocation_empty)
  private val topSpace: View = view.findViewById(R.id.chooseLocation_topSpace)
  private val bottomSpace: View = view.findViewById(R.id.chooseLocation_bottomSpace)

  private val adapter = ChooseLocationSearchAdapter()

  val events: Flow<Event>

  private var previousState: State? = null

  init {
    container.updatePaddingWithInsets(left = true, top = true, right = true, bottom = true)
    recycler.adapter = adapter
    recycler.layoutManager = LinearLayoutManager(view.context)

    events = merge(
        toolbar.navigationClicks().map { CloseClicked },
        followMeButton.clicks().map { FollowMeClicked },
        adapter.clicks().map { ResultSelected(it) },
        inputView.textChanges().map { InputSearch(it.toString()) }
    )
  }

  fun display(state: State) {

    if (previousState != state) {
      TransitionManager.beginDelayedTransition(container)
    }

    toolbar.isVisible = !state.rootScreen
    titleView.isVisible = state.loadingStatus == Idle
    followMeButton.isVisible = state.showFollowMe && state.loadingStatus == Idle
    topSpace.isVisible = state.loadingStatus == Idle
    bottomSpace.isVisible = state.loadingStatus == Idle
    recycler.isVisible = state.loadingStatus == SearchingDone && state.results.isNotEmpty()
    errorView.isVisible = state.loadingStatus == SearchingError
    emptyView.isVisible = state.loadingStatus == SearchingDone && state.results.isEmpty()
    loadingView.isVisible = state.loadingStatus == Searching

    adapter.set(state.results)

    previousState = state
  }

  fun showSelectionError() {
    Snackbar.make(view, R.string.chooseLocation_submissionError, Snackbar.LENGTH_SHORT).show()
  }

  fun showPermissionError() {
    Snackbar.make(view, R.string.chooseLocation_permissionError, Snackbar.LENGTH_SHORT).show()
  }

  sealed class Event {
    data class ResultSelected(val result: SearchResult) : Event()
    data class InputSearch(val query: String) : Event()
    object FollowMeClicked : Event()
    object CloseClicked : Event()
  }
}
