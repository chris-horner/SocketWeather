package codes.chrishorner.socketweather.widget

import androidx.annotation.DrawableRes
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.common.weatherIconRes
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.util.Strings
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle.FULL
import java.time.format.TextStyle.SHORT
import java.util.Locale

data class WidgetDateForecast(
  val day: String,
  val dayShort: String,
  @DrawableRes val iconRes: Int,
  val description: String?,
  val minTemp: Int?,
  val maxTemp: Int?,
)

data class WidgetHourlyForecast(
  val time: String,
  @DrawableRes val iconRes: Int,
  val description: String?,
  val temp: Int?
)

fun Forecast.getWidgetDateForecasts(
  strings: Strings,
  count: Int,
  clock: Clock = Clock.systemDefaultZone(),
): List<WidgetDateForecast> {
  if (count <= 0) return emptyList()

  val timezone = location.timezone
  val currentDate = LocalDate.now(clock.withZone(timezone))

  val todayEntry = WidgetDateForecast(
    day = strings[R.string.widget_today],
    dayShort = strings[R.string.widget_today],
    iconRes = weatherIconRes(todayForecast.icon_descriptor, night),
    description = todayForecast.short_text,
    minTemp = lowTemp,
    maxTemp = highTemp,
  )

  val upcomingEntries = upcomingForecasts.take(count - 1).mapIndexed { index, it ->
    val zonedDate = it.date.atZone(timezone).toLocalDate()

    val dayText = if (index == 0 && zonedDate == currentDate.plusDays(1)) {
      strings[R.string.widget_tomorrow]
    } else {
      zonedDate.dayOfWeek.getDisplayName(FULL, Locale.getDefault())
    }

    WidgetDateForecast(
      day = dayText,
      dayShort = zonedDate.dayOfWeek.getDisplayName(SHORT, Locale.getDefault()),
      iconRes = weatherIconRes(it.icon_descriptor),
      description = it.short_text,
      minTemp = it.temp_min,
      maxTemp = it.temp_max,
    )
  }

  return buildList(count) {
    add(todayEntry)
    addAll(upcomingEntries)
  }
}

fun Forecast.getWidgetHourlyForecasts(count: Int): List<WidgetHourlyForecast> {
  return hourlyForecasts.take(count).map {
    WidgetHourlyForecast(
      time = TimeFormatter.format(it.time.atZone(location.timezone)).uppercase(),
      iconRes = weatherIconRes(it.icon_descriptor, it.is_night),
      description = null, // TODO: Get this data from BOM.
      temp = it.temp,
    )
  }
}

private val TimeFormatter = DateTimeFormatter.ofPattern("h a")
