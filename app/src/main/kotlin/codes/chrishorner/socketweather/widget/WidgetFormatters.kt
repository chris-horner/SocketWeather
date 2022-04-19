package codes.chrishorner.socketweather.widget

import androidx.annotation.DrawableRes
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.common.weatherIconRes
import codes.chrishorner.socketweather.data.DateForecast
import codes.chrishorner.socketweather.util.Strings
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

fun DateForecast.formatForWidget(
  strings: Strings,
  timezone: ZoneId,
  clock: Clock = Clock.systemDefaultZone(),
): WidgetUpcomingForecast {

  val currentDate = LocalDate.now(clock.withZone(timezone))
  val zonedDate = date.atZone(timezone).toLocalDate()

  val dayText = if (zonedDate == currentDate.plusDays(1)) {
    strings[R.string.home_dateForecastTomorrow]
  } else {
    zonedDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
  }

  return WidgetUpcomingForecast(
    day = dayText,
    iconRes = weatherIconRes(icon_descriptor),
    minTemp = temp_min,
    maxTemp = temp_max,
  )
}

data class WidgetUpcomingForecast(
  val day: String,
  @DrawableRes val iconRes: Int,
  val minTemp: Int?,
  val maxTemp: Int?,
)
