package codes.chrishorner.socketweather.about

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import codes.chrishorner.socketweather.BuildConfig
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.about.AboutPresenter.Event.GoBack
import codes.chrishorner.socketweather.about.AboutPresenter.Event.OpenUrl
import codes.chrishorner.socketweather.util.updatePaddingWithInsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.appcompat.navigationClicks

class AboutPresenter(view: View) {

  val events: Flow<Event>

  init {
    val appBar: View = view.findViewById(R.id.about_appBar)
    appBar.updatePaddingWithInsets(left = true, top = true, right = true)

    val scroller: View = view.findViewById(R.id.about_scroller)
    scroller.updatePaddingWithInsets(left = true, right = true, bottom = true)

    val versionNameView: TextView = view.findViewById(R.id.about_versionName)
    versionNameView.text = BuildConfig.VERSION_NAME

    val toolbar: Toolbar = view.findViewById(R.id.about_toolbar)
    val bomButton: View = view.findViewById(R.id.about_bomButton)
    val sourceButton: View = view.findViewById(R.id.about_sourceButton)
    val websiteButton: View = view.findViewById(R.id.about_websiteButton)

    events = merge(
      toolbar.navigationClicks().map { GoBack },
      bomButton.clicks().map { OpenUrl("http://www.bom.gov.au/") },
      sourceButton.clicks().map { OpenUrl("https://github.com/chris-horner/SocketWeather") },
      websiteButton.clicks().map { OpenUrl("https://chrishorner.codes") }
    )
  }

  sealed class Event {
    object GoBack : Event()
    data class OpenUrl(val url: String) : Event()
  }
}
