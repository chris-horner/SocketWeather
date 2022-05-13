package codes.chrishorner.socketweather.widget

import androidx.annotation.DrawableRes
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.common.weatherIconRes
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.util.Strings
import java.time.Clock
import java.time.LocalDate
import java.time.format.TextStyle.FULL
import java.time.format.TextStyle.SHORT
import java.util.Locale

fun Forecast.getWidgetDateForecasts(
  strings: Strings,
  maxCount: Int,
  clock: Clock = Clock.systemDefaultZone(),
): List<WidgetDateForecast> {
  if (maxCount <= 0) return emptyList()

  val timezone = location.timezone
  val currentDate = LocalDate.now(clock.withZone(timezone))

  val todayEntry = WidgetDateForecast(
    day = strings[R.string.widget_today],
    dayShort = todayForecast.date.atZone(timezone).dayOfWeek.getDisplayName(SHORT, Locale.getDefault()),
    iconRes = weatherIconRes(todayForecast.icon_descriptor, night),
    minTemp = lowTemp,
    maxTemp = highTemp,
  )

  val upcomingEntries = upcomingForecasts.take(maxCount - 1).mapIndexed { index, it ->
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
      minTemp = it.temp_min,
      maxTemp = it.temp_max,
    )
  }

  return buildList(maxCount) {
    add(todayEntry)
    addAll(upcomingEntries)
  }
}

data class WidgetDateForecast(
  val day: String,
  val dayShort: String,
  @DrawableRes val iconRes: Int,
  val minTemp: Int?,
  val maxTemp: Int?,
)
