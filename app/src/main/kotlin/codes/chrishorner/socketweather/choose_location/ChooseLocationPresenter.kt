package codes.chrishorner.socketweather.choose_location

import android.animation.LayoutTransition
import android.animation.LayoutTransition.TransitionListener
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.FollowMeClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.ResultSelected
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Idle
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Searching
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingDone
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.SearchingError
import codes.chrishorner.socketweather.choose_location.ChooseLocationState.LoadingStatus.Submitting
import codes.chrishorner.socketweather.data.SearchResult
import codes.chrishorner.socketweather.util.dismissKeyboard
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

  private val container: ViewGroup = view.findViewById(R.id.chooseLocation_container)
  private val toolbar: Toolbar = view.findViewById(R.id.chooseLocation_toolbar)
  private val topSpace: View = view.findViewById(R.id.chooseLocation_topSpace)
  private val titleView: View = view.findViewById(R.id.chooseLocation_title)
  private val followMeButton: View = view.findViewById(R.id.chooseLocation_followMeButton)
  private val inputView: EditText = view.findViewById(R.id.chooseLocation_searchInput)
  private val recycler: RecyclerView = view.findViewById(R.id.chooseLocation_recycler)
  private val loadingView: View = view.findViewById(R.id.chooseLocation_loadingResults)
  private val errorView: View = view.findViewById(R.id.chooseLocation_error)
  private val emptyView: View = view.findViewById(R.id.chooseLocation_empty)
  private val submissionView: View = view.findViewById(R.id.chooseLocation_submitting)

  private val adapter = ChooseLocationSearchAdapter()

  val events: Flow<Event>

  init {
    container.updatePaddingWithInsets(left = true, top = true, right = true, bottom = true)
    recycler.adapter = adapter
    recycler.layoutManager = LinearLayoutManager(view.context)

    events = merge(
        toolbar.navigationClicks().map { CloseClicked },
        followMeButton.clicks().onEach { view.dismissKeyboard() }.map { FollowMeClicked },
        adapter.clicks().onEach { view.dismissKeyboard() }.map { ResultSelected(it) },
        inputView.textChanges().onEach { inputView.requestFocus() }.map { InputSearch(it.toString()) }
    )

    with(container.layoutTransition) {
      enableTransitionType(LayoutTransition.CHANGING)
      setInterpolator(LayoutTransition.CHANGE_DISAPPEARING, OvershootInterpolator(1.3f))
      setInterpolator(LayoutTransition.CHANGE_APPEARING, LinearOutSlowInInterpolator())
      setInterpolator(LayoutTransition.APPEARING, AccelerateInterpolator())
      setDuration(LayoutTransition.DISAPPEARING, 100L)
      setDuration(LayoutTransition.CHANGE_DISAPPEARING, 250L)
      setDuration(LayoutTransition.CHANGE_APPEARING, 250L)
      setDuration(LayoutTransition.APPEARING, 250L)
      setDuration(LayoutTransition.CHANGING, 250L)
    }

    // Hack around `TextInputLayout` losing focus when layout transition animations run 😢.
    container.doOnLayout {
      container.layoutTransition.addTransitionListener(object : TransitionListener {
        override fun startTransition(
            transition: LayoutTransition?,
            container: ViewGroup?,
            view: View?,
            transitionType: Int
        ) {
          inputView.requestFocus()
        }

        override fun endTransition(
            transition: LayoutTransition?,
            container: ViewGroup?,
            view: View?,
            transitionType: Int
        ) {
        }
      })
    }
  }

  fun display(state: ChooseLocationState) {
    toolbar.isVisible = state.showCloseButton
    titleView.isVisible = state.loadingStatus == Idle
    followMeButton.isVisible = state.showFollowMe && state.loadingStatus == Idle
    topSpace.isVisible = state.loadingStatus == Idle

    recycler.isVisible = state.loadingStatus == SearchingDone && state.results.isNotEmpty()
    errorView.isVisible = state.loadingStatus == SearchingError
    emptyView.isVisible = state.loadingStatus == SearchingDone && state.results.isEmpty()
    loadingView.isVisible = state.loadingStatus == Searching
    submissionView.isVisible = state.loadingStatus == Submitting

    adapter.set(state.results)
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
