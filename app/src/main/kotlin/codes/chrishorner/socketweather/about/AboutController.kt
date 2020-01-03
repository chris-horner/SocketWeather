package codes.chrishorner.socketweather.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.about.AboutPresenter.Event.GoBack
import codes.chrishorner.socketweather.about.AboutPresenter.Event.OpenUrl
import codes.chrishorner.socketweather.util.ScopedController
import codes.chrishorner.socketweather.util.inflate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AboutController : ScopedController<AboutViewModel, AboutPresenter>() {

  override fun onCreateViewModel(context: Context) = AboutViewModel()

  override fun onCreateView(container: ViewGroup): View = container.inflate(R.layout.about)

  override fun onCreatePresenter(view: View) = AboutPresenter(view)

  override fun onAttach(view: View, presenter: AboutPresenter, viewModel: AboutViewModel, viewScope: CoroutineScope) {

    presenter.events
        .onEach { event ->
          when (event) {
            GoBack -> router.popCurrentController()
            is OpenUrl -> {
              val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.url))
              startActivity(intent)
            }
          }
        }
        .launchIn(viewScope)
  }
}
