package codes.chrishorner.socketweather.choose_location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import codes.chrishorner.socketweather.HomeController
import codes.chrishorner.socketweather.R.layout
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.FollowMeClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.ResultSelected
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionSuccess
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.asTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChooseLocationController(args: Bundle) : ScopedController(args) {

  constructor(showFollowMe: Boolean, displayAsRoot: Boolean = false) : this(
      bundleOf(
          "showFollowMe" to showFollowMe,
          "displayAsRoot" to displayAsRoot
      )
  )

  private val viewModel = ChooseLocationViewModel(
      args.getBoolean("displayAsRoot"),
      args.getBoolean("showFollowMe"),
      NetworkComponents.get().api,
      LocationChoices.get()
  )

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    return inflater.inflate(layout.choose_location, container, false)
  }

  override fun onAttach(view: View, viewScope: CoroutineScope) {
    val presenter = ChooseLocationPresenter(view)

    viewModel.observeStates()
        .onEach { presenter.display(it) }
        .launchIn(viewScope)

    viewModel.observeEvents()
        .onEach { event ->
          when (event) {
            SubmissionError -> presenter.showSelectionError()
            SubmissionSuccess -> router.setRoot(HomeController().asTransaction())
          }
        }
        .launchIn(viewScope)

    presenter.events
        .onEach { event ->
          when (event) {
            is InputSearch -> viewModel.inputSearchQuery(event.query)
            is FollowMeClicked -> viewModel.selectFollowMe()
            is ResultSelected -> viewModel.selectResult(event.result)
            is CloseClicked -> router.popCurrentController()
          }
        }
        .launchIn(viewScope)
  }

  override fun onDestroy() {
    viewModel.destroy()
  }
}

