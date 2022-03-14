package codes.chrishorner.socketweather.widget

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.common.weatherIconRes
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.util.Strings.AndroidStrings
import kotlin.math.roundToInt

class ForecastWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget = ForecastWidget()
}

class ForecastWidget : GlanceAppWidget() {

  companion object {
    private val TINY_BOX = DpSize(48.dp, 48.dp)
    private val SMALL_ROW = DpSize(96.dp, 48.dp)
  }

  override val sizeMode = SizeMode.Responsive(
    setOf(TINY_BOX, SMALL_ROW)
  )

  @SuppressLint("StateFlowValueCalledInComposition") // Get the current forecast once per invalidation.
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val forecast = context.appSingletons.stores.forecast.data.value

    when (LocalSize.current) {
      TINY_BOX -> CurrentConditions(forecast)
      SMALL_ROW -> CurrentConditionsWithTemps(forecast)
    }
  }
}

private val parentModifier: GlanceModifier
  @Composable get() = GlanceModifier
    .fillMaxSize()
    .background(day = Color.White, night = Color.DarkGray)
    .appWidgetBackground()
    .appWidgetBackgroundRadius()
    .padding(8.dp)

@Composable
private fun CurrentConditions(forecast: Forecast?) {
  val strings = AndroidStrings(LocalContext.current)
  val iconRes = weatherIconRes(forecast?.iconDescriptor, night = forecast?.night ?: false)

  Column(
    modifier = parentModifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      provider = ImageProvider(iconRes),
      contentDescription = strings[R.string.widgetDescription],
      modifier = GlanceModifier.fillMaxWidth().height(44.dp),
    )
    Text(
      text = strings.formatDegrees(forecast?.currentTemp?.roundToInt()),
      maxLines = 1,
      style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
      modifier = GlanceModifier.wrapContentHeight(),
    )
  }
}

@Composable
private fun CurrentConditionsWithTemps(forecast: Forecast?) {
  val strings = AndroidStrings(LocalContext.current)
  val iconRes = weatherIconRes(forecast?.iconDescriptor, night = forecast?.night ?: false)

  Row(
    modifier = parentModifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      provider = ImageProvider(iconRes),
      contentDescription = strings[R.string.widgetDescription],
      modifier = GlanceModifier.fillMaxHeight().width(48.dp),
    )
    Spacer(modifier = GlanceModifier.width(12.dp))
    Column {
      Text(
        text = strings.formatDegrees(forecast?.currentTemp?.roundToInt()),
        maxLines = 1,
        style = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.Medium),
      )
      Row {
        Text(
          text = strings.formatDegrees(forecast?.lowTemp),
          maxLines = 1,
          style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
          text = strings.formatDegrees(forecast?.highTemp),
          maxLines = 1,
          style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
        )
      }
    }
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
