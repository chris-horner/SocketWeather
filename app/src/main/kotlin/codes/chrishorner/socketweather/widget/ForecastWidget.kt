package codes.chrishorner.socketweather.widget

import androidx.compose.runtime.Composable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.text.Text
import codes.chrishorner.socketweather.data.Forecast

class ForecastWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget = ForecastWidget(null)
}

class ForecastWidget(forecast: Forecast?) : GlanceAppWidget() {

  @Composable
  override fun Content() {
    Text("A compose widget!")
  }
}
