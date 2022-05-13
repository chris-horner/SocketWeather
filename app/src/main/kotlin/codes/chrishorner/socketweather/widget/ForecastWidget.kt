package codes.chrishorner.socketweather.widget

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.common.weatherIconRes
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.util.Strings
import codes.chrishorner.socketweather.util.Strings.AndroidStrings
import kotlin.math.roundToInt

class ForecastWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget = ForecastWidget()
}

class ForecastWidget : GlanceAppWidget() {

  companion object {
    private val WIDTH_1U = 48.dp
    private val WIDTH_2U = 96.dp
    private val WIDTH_3U = 180.dp
    private val WIDTH_4U = 220.dp
    private val WIDTH_5U = 280.dp

    private val HEIGHT_1U = 48.dp
    private val HEIGHT_2U = 160.dp
    private val HEIGHT_3U = 260.dp
    private val HEIGHT_4U = 360.dp

    private val TINY_BOX = DpSize(WIDTH_1U, HEIGHT_1U)
    private val TINY_ROW = DpSize(WIDTH_2U, HEIGHT_1U)
    private val SMALL_ROW = DpSize(WIDTH_3U, HEIGHT_1U)
    private val ROW = DpSize(WIDTH_4U, HEIGHT_1U)
    private val TINY_COLUMN = DpSize(WIDTH_1U, HEIGHT_2U)
    private val SMALL_COLUMN = DpSize(WIDTH_1U, HEIGHT_3U)
    private val COLUMN = DpSize(WIDTH_1U, HEIGHT_4U)
    private val WIDE_TINY_COLUMN = DpSize(WIDTH_2U, HEIGHT_2U)
    private val WIDE_SMALL_COLUMN = DpSize(WIDTH_2U, HEIGHT_3U)
    private val WIDE_COLUMN = DpSize(WIDTH_2U, HEIGHT_4U)
    private val SHORT_BOX = DpSize(WIDTH_2U, HEIGHT_2U)
    private val WIDE_SHORT_BOX = DpSize(WIDTH_3U, HEIGHT_2U)
    private val WIDER_SHORT_BOX = DpSize(WIDTH_5U, HEIGHT_2U)
    private val BOX = DpSize(WIDTH_4U, HEIGHT_3U)
    private val TALL_BOX = DpSize(WIDTH_4U, HEIGHT_4U)
    private val BIG_BOX = DpSize(WIDTH_5U, HEIGHT_3U)
    private val TALL_BIG_BOX = DpSize(WIDTH_5U, HEIGHT_4U)
  }

  override val sizeMode = SizeMode.Responsive(
    setOf(
      TINY_BOX,
      TINY_ROW,
      SMALL_ROW,
      ROW,
      TINY_COLUMN,
      SMALL_COLUMN,
      COLUMN,
      WIDE_TINY_COLUMN,
      WIDE_SMALL_COLUMN,
      WIDE_COLUMN,
      SHORT_BOX,
      WIDE_SHORT_BOX,
      WIDER_SHORT_BOX,
      BOX,
      TALL_BOX,
      BIG_BOX,
      TALL_BIG_BOX
    )
  )

  @SuppressLint("StateFlowValueCalledInComposition") // Get the current forecast once per invalidation.
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val forecast = context.appSingletons.stores.forecast.data.value

    CompositionLocalProvider(LocalStrings provides AndroidStrings(context)) {
      when (LocalSize.current) {
        TINY_BOX -> Column(forecast, itemCount = 1)
        TINY_ROW -> TinyRow(forecast)
        SMALL_ROW -> SmallRow(forecast)
        ROW -> Row(forecast)
        TINY_COLUMN -> Column(forecast, itemCount = 2)
        SMALL_COLUMN -> Column(forecast, itemCount = 3)
        COLUMN -> Column(forecast, itemCount = 4)
        WIDE_TINY_COLUMN -> Column(forecast, itemCount = 2, wide = true)
        WIDE_SMALL_COLUMN -> Column(forecast, itemCount = 3, wide = true)
        WIDE_COLUMN -> Column(forecast, itemCount = 4, wide = true)
        SHORT_BOX -> Column(forecast, itemCount = 2)
        WIDE_SHORT_BOX -> ShortBox(forecast, hourlyCount = 4)
        WIDER_SHORT_BOX -> ShortBox(forecast, hourlyCount = 5)
        BOX -> Box(forecast, small = true, hourlyCount = 4, dayCount = 3)
        TALL_BOX -> Box(forecast, small = true, hourlyCount = 4, dayCount = 6)
        BIG_BOX -> Box(forecast, hourlyCount = 5, dayCount = 3)
        TALL_BIG_BOX -> Box(forecast, hourlyCount = 5, dayCount = 6)
      }
    }
  }
}

private val parentModifier: GlanceModifier
  @Composable get() = GlanceModifier
    .fillMaxSize()
    .background(ImageProvider(R.drawable.bg_widget))
    .appWidgetBackground()
    .appWidgetBackgroundRadius()
    .padding(8.dp)

@Composable
private fun TinyRow(forecast: Forecast?) {
  val iconRes = weatherIconRes(forecast?.iconDescriptor, night = forecast?.night ?: false)

  Row(
    modifier = parentModifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      provider = ImageProvider(iconRes),
      contentDescription = forecast?.todayForecast?.short_text,
      modifier = GlanceModifier.fillMaxHeight().width(48.dp),
    )
    Spacer(modifier = GlanceModifier.width(12.dp))
    Column {
      LargeTemp(forecast?.currentTemp?.roundToInt())
      Row {
        SmallTemp(forecast?.lowTemp)
        Spacer(modifier = GlanceModifier.width(8.dp))
        SmallTemp(forecast?.highTemp)
      }
    }
  }
}

@Composable
private fun SmallRow(forecast: Forecast?) {
  Box(modifier = parentModifier.padding(8.dp)) {
    SmallCurrentConditionsRow(forecast)
  }
}

@Composable
private fun Row(forecast: Forecast?) {
  Box(modifier = parentModifier.padding(8.dp), contentAlignment = Alignment.Center) {
    CurrentConditionsRow(forecast)
  }
}

@Composable
private fun Column(forecast: Forecast?, itemCount: Int, wide: Boolean = false) {
  val dayForecasts = forecast?.upcomingForecasts.orEmpty()
  val maxDays = itemCount.coerceAtMost(dayForecasts.size + 1) // +1 to account for today's forecast.
  // Glance only supports up to 10 children per container. Because we want to space entries evenly, we can only have
  // up to 4 entries, each surrounded by Spacer widgets.
  val rowCount = maxDays.coerceAtMost(4)
  val entries = forecast?.getWidgetDateForecasts(LocalStrings.current, rowCount) ?: emptyList()

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
private fun ShortBox(forecast: Forecast?, small: Boolean = false, hourlyCount: Int) {
  Column(modifier = parentModifier.padding(8.dp)) {
    if (small) {
      SmallCurrentConditionsRow(forecast)
    } else {
      CurrentConditionsRow(forecast)
    }
    Spacer(modifier = GlanceModifier.defaultWeight())
    HourlyForecastRow(forecast, entryCount = hourlyCount)
  }
}

@Composable
private fun Box(forecast: Forecast?, small: Boolean = false, hourlyCount: Int, dayCount: Int) {

  val dayForecasts = forecast?.upcomingForecasts.orEmpty()
  val maxDays = dayCount.coerceAtMost(dayForecasts.size)
  val rowCount = maxDays.coerceAtMost(10) // Glance only supports up to 10 children per container.

  val upcomingForecasts = forecast?.getWidgetDateForecasts(LocalStrings.current, count = rowCount + 1)
    ?.drop(1)
    ?: emptyList()

  Column(modifier = parentModifier.padding(8.dp)) {
    CurrentConditionsRow(forecast)
    Spacer(modifier = GlanceModifier.defaultWeight())
    HourlyForecastRow(forecast, entryCount = hourlyCount)
    Spacer(modifier = GlanceModifier.defaultWeight())
    Column(
      modifier = GlanceModifier
        .background(ImageProvider(R.drawable.bg_widget_inner))
        .padding(start = 16.dp, end = 16.dp, top = 12.dp)
    ) {
      upcomingForecasts.take(rowCount).forEach { upcomingForecast ->
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
private fun CurrentConditionsRow(forecast: Forecast?) {
  val strings = LocalStrings.current
  val iconRes = weatherIconRes(forecast?.iconDescriptor, night = forecast?.night ?: false)

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = GlanceModifier.fillMaxWidth(),
  ) {
    Column(modifier = GlanceModifier.defaultWeight().padding(top = 4.dp)) {
      TitleText(forecast?.location?.name ?: "", fontWeight = FontWeight.Medium)
      SmallText(forecast?.todayForecast?.short_text ?: "")
      SmallText(strings.get(R.string.widget_feels_long, strings.formatDegrees(forecast?.tempFeelsLike?.roundToInt())))
    }
    Column(horizontalAlignment = Alignment.End) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
          provider = ImageProvider(iconRes),
          contentDescription = forecast?.todayForecast?.short_text,
          modifier = GlanceModifier.size(40.dp),
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        LargeTemp(temp = forecast?.currentTemp?.roundToInt())
      }
      HorizontalLowToHighTemps(forecast, modifier = GlanceModifier.width(80.dp))
    }
  }
}

@Composable
private fun SmallCurrentConditionsRow(forecast: Forecast?) {
  val strings = LocalStrings.current
  val iconRes = weatherIconRes(forecast?.iconDescriptor, night = forecast?.night ?: false)

  Row(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
    modifier = GlanceModifier.fillMaxWidth(),
  ) {
    Image(
      provider = ImageProvider(iconRes),
      contentDescription = forecast?.todayForecast?.short_text,
      modifier = GlanceModifier.size(56.dp)
    )
    Spacer(modifier = GlanceModifier.defaultWeight())
    Column(
      modifier = GlanceModifier,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      LargeTemp(forecast?.currentTemp?.roundToInt())
      Spacer(modifier = GlanceModifier.defaultWeight())
      SmallText(strings.get(R.string.widget_feels_short, strings.formatDegrees(forecast?.tempFeelsLike?.roundToInt())))
    }
    Spacer(modifier = GlanceModifier.defaultWeight())
    VerticalLowToHighTemps(forecast, modifier = GlanceModifier.padding(top = 6.dp))
  }
}

@Composable
private fun HourlyForecastRow(forecast: Forecast?, entryCount: Int) {
  Row {
    forecast?.getWidgetHourlyForecasts(entryCount)
      ?.forEachIndexed { index, entry ->
        val padding = if (index != 0) 24.dp else 0.dp
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
    // TODO: Update widths to use sp.
    RegularTemp(forecast.minTemp, modifier = GlanceModifier.width(28.dp))
    Image(
      provider = ImageProvider(R.drawable.bg_widget_line),
      contentDescription = null,
      modifier = GlanceModifier.height(4.dp).width(32.dp).padding(horizontal = 6.dp),
    )
    RegularTemp(forecast.maxTemp, modifier = GlanceModifier.width(28.dp))
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
  }
}

@Composable
private fun VerticalLowToHighTemps(forecast: Forecast?, modifier: GlanceModifier = GlanceModifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    SmallTemp(temp = forecast?.highTemp)
    Image(
      provider = ImageProvider(R.drawable.bg_widget_line),
      contentDescription = null,
      modifier = GlanceModifier.width(4.dp).height(28.dp).padding(vertical = 6.dp),
    )
    SmallTemp(forecast?.lowTemp)
  }
}

@Composable
private fun HorizontalLowToHighTemps(forecast: Forecast?, modifier: GlanceModifier = GlanceModifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    RegularTemp(temp = forecast?.lowTemp)
    Image(
      provider = ImageProvider(R.drawable.bg_widget_line),
      contentDescription = null,
      modifier = GlanceModifier.height(4.dp).defaultWeight().padding(horizontal = 6.dp),
    )
    RegularTemp(forecast?.highTemp)
  }
}

@Composable
private fun HourlyForecastEntry(entry: WidgetHourlyForecast, modifier: GlanceModifier = GlanceModifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    SmallText(entry.time)
    Image(
      provider = ImageProvider(entry.iconRes),
      contentDescription = entry.description,
      modifier = GlanceModifier.size(32.dp).padding(vertical = 4.dp)
    )
    SmallTemp(entry.temp)
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
private fun TinyTemp(temp: Int?) {
  Text(
    text = LocalStrings.current.formatDegrees(temp),
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
private fun SmallTemp(temp: Int?, modifier: GlanceModifier = GlanceModifier) {
  Text(
    text = LocalStrings.current.formatDegrees(temp),
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
private fun RegularTemp(temp: Int?, modifier: GlanceModifier = GlanceModifier) {
  Text(
    text = LocalStrings.current.formatDegrees(temp),
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
private fun LargeTemp(temp: Int?, modifier: GlanceModifier = GlanceModifier) {
  Text(
    text = LocalStrings.current.formatDegrees(temp),
    maxLines = 1,
    style = TextStyle(
      fontSize = 34.sp,
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
      fontSize = 18.sp,
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

private val LocalStrings = staticCompositionLocalOf<Strings> { error("No Strings provided.") }
