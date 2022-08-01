package codes.chrishorner.socketweather.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import codes.chrishorner.socketweather.MainActivity
import codes.chrishorner.socketweather.R

private val parentModifier: GlanceModifier
  @Composable get() = GlanceModifier
    .fillMaxSize()
    .background(ImageProvider(R.drawable.bg_widget))
    .appWidgetBackground()
    .appWidgetBackgroundRadius()
    .padding(8.dp)
    .clickable(onClick = actionStartActivity<MainActivity>())

@Composable
fun TinyRow(conditions: WidgetCurrentConditions) {
  Row(
    modifier = parentModifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      provider = ImageProvider(conditions.iconRes),
      contentDescription = conditions.description,
      modifier = GlanceModifier.fillMaxHeight().width(48.dp),
    )
    Spacer(modifier = GlanceModifier.width(12.dp))
    Column {
      LargeTemp(conditions.currentTemp)
      Row {
        SmallTemp(conditions.minTemp)
        Spacer(modifier = GlanceModifier.width(8.dp))
        SmallTemp(conditions.maxTemp)
      }
    }
  }
}

@Composable
fun SmallRow(conditions: WidgetCurrentConditions) {
  Box(modifier = parentModifier.padding(8.dp), contentAlignment = Alignment.Center) {
    SmallCurrentConditionsRow(conditions)
  }
}

@Composable
fun Row(conditions: WidgetCurrentConditions) {
  Box(modifier = parentModifier.padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
    CurrentConditionsRow(conditions)
  }
}

@Composable
fun Column(dateForecasts: List<WidgetDateForecast>, itemCount: Int, wide: Boolean = false) {
  // Glance only supports up to 10 children per container. Because we want to space entries evenly, we can only have
  // up to 4 entries, each surrounded by Spacer widgets.
  val rowCount = itemCount.coerceAtMost(4)
  val entries = dateForecasts.take(rowCount)

  Column(
    modifier = parentModifier.padding(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(modifier = GlanceModifier.defaultWeight())
    for (entry in entries) {
      if (wide) {
        WideColumnEntry(entry)
      } else {
        ColumnEntry(entry)
      }
      Spacer(modifier = GlanceModifier.defaultWeight())
    }
  }
}

@Composable
fun ShortBox(forecast: WidgetForecast, small: Boolean = false, hourlyCount: Int) {
  Column(modifier = parentModifier.padding(8.dp)) {
    if (small) {
      SmallCurrentConditionsRow(forecast.currentConditions)
    } else {
      CurrentConditionsRow(forecast.currentConditions)
    }
    Spacer(modifier = GlanceModifier.defaultWeight())
    HourlyForecastRow(forecast.hourlyForecasts.take(hourlyCount))
  }
}

@Composable
fun Box(forecast: WidgetForecast, small: Boolean = false, hourlyCount: Int, dayCount: Int) {
  val rowCount = dayCount.coerceAtMost(10) // Glance only supports up to 10 children per container.

  Column(modifier = parentModifier.padding(8.dp)) {
    CurrentConditionsRow(forecast.currentConditions)
    Spacer(modifier = GlanceModifier.defaultWeight())
    HourlyForecastRow(forecast.hourlyForecasts.take(hourlyCount))
    Spacer(modifier = GlanceModifier.defaultWeight())
    Column(
      modifier = GlanceModifier
        .background(ImageProvider(R.drawable.bg_widget_inner))
        .padding(start = 16.dp, end = 16.dp, top = 12.dp)
    ) {
      // Drop the first date, since we display today's forecast in elements above.
      forecast.dateForecasts.drop(1).take(rowCount).forEach { upcomingForecast ->
        UpcomingForecastRow(
          forecast = upcomingForecast,
          isSmall = small,
          modifier = GlanceModifier.padding(bottom = 12.dp),
        )
      }
    }
  }
}

@Composable
private fun CurrentConditionsRow(conditions: WidgetCurrentConditions) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = GlanceModifier.fillMaxWidth(),
  ) {
    Column(modifier = GlanceModifier.defaultWeight().padding(top = 4.dp)) {
      TitleText(conditions.location, fontWeight = FontWeight.Medium)
      RegularText(conditions.description)
    }
    Column(horizontalAlignment = Alignment.End) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          provider = ImageProvider(conditions.iconRes),
          contentDescription = conditions.description,
          modifier = GlanceModifier.size(40.dp),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        LargeTemp(conditions.currentTemp)
      }
      HorizontalLowToHighTemps(conditions, modifier = GlanceModifier.width(80.dp))
    }
  }
}

@Composable
private fun SmallCurrentConditionsRow(conditions: WidgetCurrentConditions) {
  Row(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
    modifier = GlanceModifier.fillMaxWidth(),
  ) {
    Image(
      provider = ImageProvider(conditions.iconRes),
      contentDescription = conditions.description,
      modifier = GlanceModifier.size(56.dp)
    )
    Spacer(modifier = GlanceModifier.defaultWeight())
    Column(
      modifier = GlanceModifier,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      LargeTemp(conditions.currentTemp)
      Spacer(modifier = GlanceModifier.defaultWeight())
      SmallText(conditions.feelsLikeText)
    }
    Spacer(modifier = GlanceModifier.defaultWeight())
    VerticalLowToHighTemps(conditions, modifier = GlanceModifier.padding(top = 6.dp))
  }
}

@Composable
private fun HourlyForecastRow(hourlyForecasts: List<WidgetHourlyForecast>) {
  Row {
    hourlyForecasts.forEachIndexed { index, entry ->
      val padding = if (index != 0) 32.dp else 0.dp
      HourlyForecastEntry(
        entry,
        modifier = GlanceModifier.padding(start = padding),
      )
    }
  }
}

@Composable
private fun UpcomingForecastRow(
  forecast: WidgetDateForecast,
  isSmall: Boolean = false,
  modifier: GlanceModifier = GlanceModifier,
) {
  // The width of the temperature text needs to scale with SP.
  val temperatureWidth = with(Density(LocalContext.current)) {
    30.sp.toDp()
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.fillMaxWidth(),
  ) {
    RegularText(if (isSmall) forecast.dayShort else forecast.day)
    Spacer(modifier = GlanceModifier.defaultWeight())
    Image(
      provider = ImageProvider(forecast.iconRes),
      contentDescription = forecast.description,
      modifier = GlanceModifier.size(24.dp),
    )
    Spacer(modifier = GlanceModifier.width(12.dp))
    RegularTemp(forecast.minTemp, modifier = GlanceModifier.width(temperatureWidth))
    Image(
      provider = ImageProvider(R.drawable.bg_widget_line),
      contentDescription = null,
      modifier = GlanceModifier.height(4.dp).width(32.dp).padding(horizontal = 6.dp),
    )
    RegularTemp(forecast.maxTemp, modifier = GlanceModifier.width(temperatureWidth))
  }
}

@Composable
private fun ColumnEntry(dateForecast: WidgetDateForecast) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    TinyTitle(dateForecast.dayShort.uppercase())
    Image(
      provider = ImageProvider(dateForecast.iconRes),
      contentDescription = dateForecast.description,
      modifier = GlanceModifier.fillMaxWidth().height(36.dp),
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      TinyTemp(dateForecast.minTemp)
      Spacer(modifier = GlanceModifier.width(4.dp))
      TinyTemp(dateForecast.maxTemp)
    }
  }
}

@Composable
private fun WideColumnEntry(dateForecast: WidgetDateForecast) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = GlanceModifier.fillMaxWidth(),
  ) {
    Spacer(modifier = GlanceModifier.defaultWeight())
    Image(
      provider = ImageProvider(dateForecast.iconRes),
      contentDescription = dateForecast.description,
      modifier = GlanceModifier.size(44.dp),
    )
    Spacer(modifier = GlanceModifier.defaultWeight())
    Column {
      RegularText(dateForecast.day)
      Row(verticalAlignment = Alignment.CenterVertically) {
        RegularTemp(dateForecast.minTemp)
        Image(
          provider = ImageProvider(R.drawable.bg_widget_line),
          contentDescription = null,
          modifier = GlanceModifier.width(32.dp).height(4.dp).padding(horizontal = 6.dp),
        )
        RegularTemp(dateForecast.maxTemp)
      }
    }
    Spacer(modifier = GlanceModifier.defaultWeight())
  }
}

@Composable
private fun VerticalLowToHighTemps(conditions: WidgetCurrentConditions, modifier: GlanceModifier = GlanceModifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    SmallTemp(conditions.minTemp)
    Image(
      provider = ImageProvider(R.drawable.bg_widget_line),
      contentDescription = null,
      modifier = GlanceModifier.width(4.dp).height(28.dp).padding(vertical = 6.dp),
    )
    SmallTemp(conditions.maxTemp)
  }
}

@Composable
private fun HorizontalLowToHighTemps(conditions: WidgetCurrentConditions, modifier: GlanceModifier = GlanceModifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    RegularTemp(conditions.minTemp)
    Image(
      provider = ImageProvider(R.drawable.bg_widget_line),
      contentDescription = null,
      modifier = GlanceModifier.height(4.dp).defaultWeight().padding(horizontal = 6.dp),
    )
    RegularTemp(conditions.maxTemp)
  }
}

@Composable
private fun HourlyForecastEntry(entry: WidgetHourlyForecast, modifier: GlanceModifier = GlanceModifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    RegularText(entry.time)
    Image(
      provider = ImageProvider(entry.iconRes),
      contentDescription = entry.description,
      modifier = GlanceModifier.size(32.dp).padding(vertical = 4.dp)
    )
    RegularTemp(entry.temp)
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

@Composable
private fun TinyTemp(text: String) {
  Text(
    text = text,
    maxLines = 1,
    style = TextStyle(
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium,
      color = ColorProvider(R.color.widgetOnBackground),
    ),
  )
}

@Composable
private fun TinyTitle(text: String) {
  Text(
    text = text,
    style = TextStyle(
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = ColorProvider(R.color.widgetOnBackground)
    ),
  )
}

@Composable
private fun SmallTemp(text: String, modifier: GlanceModifier = GlanceModifier) {
  Text(
    text = text,
    maxLines = 1,
    style = TextStyle(
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium,
      color = ColorProvider(R.color.widgetOnBackground),
    ),
    modifier = modifier
  )
}

@Composable
private fun SmallText(
  text: String,
  modifier: GlanceModifier = GlanceModifier,
  fontWeight: FontWeight = FontWeight.Normal,
) {
  Text(
    text = text,
    style = TextStyle(
      fontSize = 14.sp,
      fontWeight = fontWeight,
      color = ColorProvider(R.color.widgetOnBackgroundSecondary),
    ),
    modifier = modifier,
  )
}

@Composable
private fun RegularTemp(text: String, modifier: GlanceModifier = GlanceModifier) {
  Text(
    text = text,
    maxLines = 1,
    style = TextStyle(
      fontSize = 18.sp,
      textAlign = TextAlign.End,
      fontWeight = FontWeight.Medium,
      color = ColorProvider(R.color.widgetOnBackground),
    ),
    modifier = modifier,
  )
}

@Composable
private fun LargeTemp(text: String, modifier: GlanceModifier = GlanceModifier) {
  Text(
    text = text,
    maxLines = 1,
    style = TextStyle(
      fontSize = 34.dpAsSp,
      fontWeight = FontWeight.Medium,
      color = ColorProvider(R.color.widgetAccent),
    ),
    modifier = modifier,
  )
}

@Composable
private fun RegularText(
  text: String,
  modifier: GlanceModifier = GlanceModifier,
  fontWeight: FontWeight = FontWeight.Normal,
) {
  Text(
    text = text,
    style = TextStyle(
      fontSize = 16.sp,
      fontWeight = fontWeight,
      color = ColorProvider(R.color.widgetOnBackgroundSecondary),
    ),
    modifier = modifier,
  )
}

@Composable
private fun TitleText(
  text: String,
  modifier: GlanceModifier = GlanceModifier,
  fontWeight: FontWeight = FontWeight.Normal,
) {
  Text(
    text = text,
    style = TextStyle(
      fontSize = 20.sp,
      fontWeight = fontWeight,
      color = ColorProvider(R.color.widgetAccent),
    ),
    modifier = modifier,
  )
}

private val Int.dpAsSp
  @Composable
  get(): TextUnit = with(Density(LocalContext.current)) { this@dpAsSp.dp.toSp() }
