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
import kotlin.math.roundToInt

data class WidgetForecast(
  val currentConditions: WidgetCurrentConditions,
  val hourlyForecasts: List<WidgetHourlyForecast>,
  val dateForecasts: List<WidgetDateForecast>,
)

data class WidgetCurrentConditions(
  @DrawableRes val iconRes: Int,
  val location: String,
  val description: String,
  val feelsLikeText: String,
  val currentTemp: String,
  val minTemp: String,
  val maxTemp: String,
)

data class WidgetHourlyForecast(
  val time: String,
  @DrawableRes val iconRes: Int,
  val description: String?,
  val temp: String,
)

data class WidgetDateForecast(
  val day: String,
  val dayShort: String,
  @DrawableRes val iconRes: Int,
  val description: String?,
  val minTemp: String,
  val maxTemp: String,
)

fun Forecast.formatForWidget(
  strings: Strings,
  clock: Clock = Clock.systemDefaultZone(),
) = WidgetForecast(
  currentConditions = getWidgetCurrentConditions(strings),
  hourlyForecasts = getWidgetHourlyForecasts(strings),
  dateForecasts = getWidgetDateForecasts(strings, clock),
)

private fun Forecast.getWidgetDateForecasts(
  strings: Strings,
  clock: Clock = Clock.systemDefaultZone(),
): List<WidgetDateForecast> {
  val timezone = location.timezone
  val currentDate = LocalDate.now(clock.withZone(timezone))

  val todayEntry = WidgetDateForecast(
    day = strings[R.string.widget_today],
    dayShort = strings[R.string.widget_today],
    iconRes = weatherIconRes(todayForecast.icon_descriptor, night),
    description = todayForecast.short_text,
    minTemp = strings.formatDegrees(lowTemp),
    maxTemp = strings.formatDegrees(highTemp),
  )

  val upcomingEntries = upcomingForecasts.mapIndexed { index, it ->
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
      minTemp = strings.formatDegrees(it.temp_min),
      maxTemp = strings.formatDegrees(it.temp_max),
    )
  }

  return listOf(todayEntry) + upcomingEntries
}

private fun Forecast.getWidgetHourlyForecasts(strings: Strings): List<WidgetHourlyForecast> {
  return hourlyForecasts.map {
    WidgetHourlyForecast(
      time = TimeFormatter.format(it.time.atZone(location.timezone)).uppercase(),
      iconRes = weatherIconRes(it.icon_descriptor, it.is_night),
      description = null, // TODO: Get this data from BOM.
      temp = strings.formatDegrees(it.temp),
    )
  }
}

private fun Forecast.getWidgetCurrentConditions(strings: Strings): WidgetCurrentConditions {
  return WidgetCurrentConditions(
    iconRes = weatherIconRes(iconDescriptor, night = night),
    location = location.name,
    description = todayForecast.short_text ?: "",
    feelsLikeText = strings.get(R.string.widget_feels_long, strings.formatDegrees(tempFeelsLike?.roundToInt())),
    currentTemp = strings.formatDegrees(currentTemp.roundToInt()),
    minTemp = strings.formatDegrees(lowTemp),
    maxTemp = strings.formatDegrees(highTemp),
  )
}

private val TimeFormatter = DateTimeFormatter.ofPattern("h a")
