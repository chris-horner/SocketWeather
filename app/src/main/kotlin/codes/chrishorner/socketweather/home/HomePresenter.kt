package codes.chrishorner.socketweather.home

import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecaster.State
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.home.HomePresenter.Event.AboutClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.RefreshClicked
import codes.chrishorner.socketweather.home.HomePresenter.Event.SwitchLocationClicked
import codes.chrishorner.socketweather.util.setDisplayedChildId
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import org.threeten.bp.Duration
import org.threeten.bp.Instant
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.appcompat.itemClicks

class HomePresenter(view: View) {

  enum class Event { SwitchLocationClicked, RefreshClicked, AboutClicked }

  private val toolbar: Toolbar = view.findViewById(R.id.home_toolbar)
  private val locationDropdown: View = view.findViewById(R.id.home_locationDropdown)
  private val toolbarTitle: TextView = view.findViewById(R.id.home_toolbarTitle)
  private val toolbarSubtitle: TextView = view.findViewById(R.id.home_toolbarSubtitle)
  private val secondaryLoading: View = view.findViewById(R.id.home_secondaryLoading)
  private val contentContainer: ViewFlipper = view.findViewById(R.id.home_contentContainer)
  private val retryButton: View = view.findViewById(R.id.home_error_retryButton)

  private val forecastPresenter = HomeForecastPresenter(view.findViewById(R.id.home_forecast))
  private val errorPresenter = HomeErrorPresenter(view.findViewById(R.id.home_error))

  private val context: Context = view.context

  val events: Flow<Event>

  private var currentState: State? = null

  init {
    toolbar.updatePaddingWithInsets(left = true, top = true, right = true)
    val scroller: View = view.findViewById(R.id.home_scroller)
    scroller.updatePaddingWithInsets(bottom = true)

    val menuEvents: Flow<Event> = toolbar.itemClicks().map {
      when (it.itemId) {
        R.id.menu_refresh -> RefreshClicked
        R.id.menu_about -> AboutClicked
        else -> throw IllegalArgumentException("Unknown item selected.")
      }
    }

    events = merge(
        locationDropdown.clicks().map { SwitchLocationClicked },
        retryButton.clicks().map { RefreshClicked },
        menuEvents
    )
  }

  fun display(state: State) {

    when (state) {
      is State.Idle -> {
        contentContainer.setDisplayedChildId(R.id.home_loading)
        toolbarTitle.setText(R.string.home_loading)
      }

      is State.FindingLocation -> {
        contentContainer.setDisplayedChildId(R.id.home_loading)
        toolbarTitle.setText(R.string.home_findingLocation)
      }

      is State.LoadingForecast -> {
        contentContainer.setDisplayedChildId(R.id.home_loading)
        toolbarTitle.setText(R.string.home_loading)
      }

      is State.Error -> {
        contentContainer.setDisplayedChildId(R.id.home_error)
        val title: String = when (val selection = state.selection) {
          is LocationSelection.Static -> selection.location.name
          is LocationSelection.FollowMe -> context.getString(R.string.home_findingLocation)
          is LocationSelection.None -> throw IllegalStateException("Cannot display LocationSelection of None.")
        }

        toolbarTitle.text = title
        errorPresenter.display(state)
      }

      is State.Refreshing -> {
        contentContainer.setDisplayedChildId(R.id.home_forecast)
        toolbarTitle.text = state.previousForecast.location.name
        forecastPresenter.display(state.previousForecast)
      }

      is State.Loaded -> {
        contentContainer.setDisplayedChildId(R.id.home_forecast)
        toolbarTitle.text = state.forecast.location.name
        forecastPresenter.display(state.forecast)
      }
    }

    // The secondary loading view (the little progress bar in the Toolbar) should only
    // be visible when we're refreshing.
    secondaryLoading.isVisible = state is State.Refreshing

    currentState = state
    updateRefreshTimeText()
  }

  /**
   * Updates the text that appears below the Toolbar title on the home screen.
   *
   * This method relies on `display(state)` being called first.
   */
  fun updateRefreshTimeText() {

    when (val state = currentState) {

      is State.Refreshing -> {
        toolbarSubtitle.isVisible = true
        toolbarSubtitle.setText(R.string.home_updatingNow)
      }

      is State.Loaded -> {
        toolbarSubtitle.isVisible = true
        val updateTime = state.forecast.updateTime
        val now = Instant.now()

        if (Duration.between(updateTime, now).toMinutes() > 0) {
          val timeAgoText = DateUtils.getRelativeTimeSpanString(updateTime.toEpochMilli())
          toolbarSubtitle.text = context.getString(R.string.home_lastUpdated, timeAgoText)
        } else {
          toolbarSubtitle.setText(R.string.home_justUpdated)
        }
      }

      else -> toolbarSubtitle.isVisible = false
    }
  }
}
