package codes.chrishorner.socketweather.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import codes.chrishorner.socketweather.appSingletons

class ForecastWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget = ForecastWidget()
}

class ForecastWidget : GlanceAppWidget() {

  @Composable
  override fun Content() {

    val context = LocalContext.current
    val forecast = context.appSingletons.stores.forecast.data.value

    Text(
      text = "Weather for ${forecast?.location?.name}",
      modifier = GlanceModifier
        .fillMaxSize()
        .background(day = Color.White, night = Color.DarkGray)
        .appWidgetBackground()
        .appWidgetBackgroundRadius()
        .padding(8.dp)
    )
  }
}

@Composable
private fun GlanceModifier.appWidgetBackgroundRadius(): GlanceModifier {
  return if (Build.VERSION.SDK_INT >= 31) {
    this.cornerRadius(android.R.dimen.system_app_widget_background_radius)
  } else {
    this.cornerRadius(16.dp)
  }
}
