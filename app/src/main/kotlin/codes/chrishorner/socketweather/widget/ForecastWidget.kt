package codes.chrishorner.socketweather.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import codes.chrishorner.socketweather.data.Forecast

class ForecastWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget = ForecastWidget(null)
}

class ForecastWidget(forecast: Forecast?) : GlanceAppWidget() {

  @Composable
  override fun Content() {
    Text(
      text = "A compose widget!",
      modifier = GlanceModifier
        .fillMaxSize()
        .background(day = Color.White, night = Color.DarkGray)
        .appWidgetBackground()
        .cornerRadius(16.dp)
        .padding(8.dp)
    )
  }
}
