package codes.chrishorner.socketweather.about

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import codes.chrishorner.socketweather.Navigator
import codes.chrishorner.socketweather.Presenter
import codes.chrishorner.socketweather.about.AboutPresenter.BackPressEvent
import codes.chrishorner.socketweather.util.CollectEffect
import kotlinx.coroutines.flow.Flow

class AboutPresenter(private val navigator: Navigator) : Presenter<BackPressEvent, Unit> {
  @SuppressLint("ComposableNaming") // About screen is so simple that it has no state to emit.
  @Composable
  override fun states(events: Flow<BackPressEvent>) {
    CollectEffect(events) { navigator.pop() }
  }

  object BackPressEvent
}
