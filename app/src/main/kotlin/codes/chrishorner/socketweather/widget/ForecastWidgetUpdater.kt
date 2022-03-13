package codes.chrishorner.socketweather.widget

import android.app.Application
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

interface ForecastWidgetUpdater {
  fun update()
}

class RealForecastWidgetUpdater(private val app: Application) : ForecastWidgetUpdater {

  private val scope = MainScope()

  override fun update() {
    scope.launch {
      ForecastWidget().updateAll(app)
    }
  }
}
