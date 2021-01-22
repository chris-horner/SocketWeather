package codes.chrishorner.socketweather.choose_location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.CloseClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.FollowMeClicked
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.InputSearch
import codes.chrishorner.socketweather.choose_location.ChooseLocationPresenter.Event.ResultSelected
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.PermissionError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionError
import codes.chrishorner.socketweather.choose_location.ChooseLocationViewModel.Event.SubmissionSuccess
import codes.chrishorner.socketweather.home.HomeController
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.asTransaction
import codes.chrishorner.socketweather.util.dismissKeyboard
import codes.chrishorner.socketweather.util.inflate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChooseLocationController(
    args: Bundle
) : ScopedController<ChooseLocationViewModel, ChooseLocationPresenter>(args) {

  constructor(displayAsRoot: Boolean = false) : this(bundleOf("displayAsRoot" to displayAsRoot))

  override fun onCreateView(container: ViewGroup): View = container.inflate(R.layout.choose_location)

  override fun onCreateViewModel(context: Context) = ChooseLocationViewModel(
      args.getBoolean("displayAsRoot"),
      context.appSingletons.networkComponents.api,
      context.appSingletons.locationChoices
  )

  override fun onCreatePresenter(view: View, viewModel: ChooseLocationViewModel) = ChooseLocationPresenter(view)

  override fun onAttach(
      view: View,
      presenter: ChooseLocationPresenter,
      viewModel: ChooseLocationViewModel,
      viewScope: CoroutineScope
  ) {

    viewModel.states
        .onEach { presenter.display(it) }
        .launchIn(viewScope)

    viewModel.observeEvents()
        .onEach { event ->
          when (event) {
            SubmissionError -> presenter.showSelectionError()
            PermissionError -> presenter.showPermissionError()
            SubmissionSuccess -> router.setRoot(HomeController().asTransaction())
          }
        }
        .launchIn(viewScope)

    presenter.events
        .onEach { event ->
          when (event) {
            is InputSearch -> viewModel.inputSearchQuery(event.query)
            is FollowMeClicked -> processFollowMe(viewModel)
            is ResultSelected -> viewModel.selectResult(event.result)
            is CloseClicked -> {
              view.dismissKeyboard()
              router.popCurrentController()
            }
          }
        }
        .launchIn(viewScope)
  }

  private fun processFollowMe(viewModel: ChooseLocationViewModel) {
    val activity = requireNotNull(activity)
    val locationPermission: Int = activity.checkSelfPermission(ACCESS_COARSE_LOCATION)

    if (locationPermission != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(arrayOf(ACCESS_COARSE_LOCATION), 0)
    } else {
      viewModel.selectFollowMe(true)
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    getViewModel()?.selectFollowMe(grantResults.contains(PackageManager.PERMISSION_GRANTED))
  }

  override fun onDestroy(viewModel: ChooseLocationViewModel?) {
    viewModel?.viewModelScope?.cancel()
  }
}
