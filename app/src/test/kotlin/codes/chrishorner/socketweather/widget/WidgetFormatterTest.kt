package codes.chrishorner.socketweather.widget

import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.CurrentInformation
import codes.chrishorner.socketweather.data.DateForecast
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.HourlyForecast
import codes.chrishorner.socketweather.data.Rain
import codes.chrishorner.socketweather.data.Rain.Amount
import codes.chrishorner.socketweather.data.Uv
import codes.chrishorner.socketweather.data.Wind
import codes.chrishorner.socketweather.test.DefaultLocaleRule
import codes.chrishorner.socketweather.test.FakeStrings
import codes.chrishorner.socketweather.test.TestData
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class WidgetFormatterTest {

  @get:Rule val localeRule = DefaultLocaleRule(Locale.forLanguageTag("en-AU"))

  private val forecast = Forecast(
    updateTime = ZonedDateTime.of(2022, 7, 1, 19, 30, 0, 0, ZoneId.of("Australia/Melbourne")).toInstant(),
    location = TestData.location1,
    iconDescriptor = "snow",
    night = false,
    currentTemp = -5.5f,
    tempFeelsLike = -6f,
    humidity = 80,
    wind = Wind(3, "E"),
    highTemp = 3,
    lowTemp = -7,
    todayForecast = DateForecast(
      date = ZonedDateTime.of(2022, 7, 1, 19, 30, 0, 0, ZoneId.of("Australia/Melbourne")).toInstant(),
      temp_min = -7,
      temp_max = 3,
      extended_text = "Lots of snow. Go outside an play.",
      short_text = "Snow",
      icon_descriptor = "snow",
      rain = Rain(Amount(null, null, "mm"), 0),
      uv = Uv(null, null, null),
      now = CurrentInformation(
        is_night = true,
        now_label = "Snow",
        later_label = "Snow",
        temp_now = -5,
        temp_later = -7,
      ),
    ),
    hourlyForecasts = listOf(
      HourlyForecast(
        rain = Rain(Amount(null, null, "mm"), 0),
        temp = -5,
        wind = Wind(3, "E"),
        icon_descriptor = "snow",
        time = ZonedDateTime.of(2022, 7, 1, 19, 30, 0, 0, ZoneId.of("Australia/Melbourne")).toInstant(),
        is_night = false,
      ),
      HourlyForecast(
        rain = Rain(Amount(null, null, "mm"), 0),
        temp = -4,
        wind = Wind(2, "N"),
        icon_descriptor = "snow",
        time = ZonedDateTime.of(2022, 7, 1, 22, 0, 0, 0, ZoneId.of("Australia/Melbourne")).toInstant(),
        is_night = true,
      )
    ),
    upcomingForecasts = listOf(
      DateForecast(
        date = ZonedDateTime.of(2022, 7, 2, 19, 30, 0, 0, ZoneId.of("Australia/Melbourne")).toInstant(),
        temp_min = -6,
        temp_max = 4,
        extended_text = "Still more snow. Go snowboarding or something.",
        short_text = "Snow",
        icon_descriptor = "snow",
        rain = Rain(Amount(null, null, "mm"), 0),
        uv = Uv(null, null, null),
        now = null,
      ),
      DateForecast(
        date = ZonedDateTime.of(2022, 7, 3, 19, 30, 0, 0, ZoneId.of("Australia/Melbourne")).toInstant(),
        temp_min = -5,
        temp_max = 3,
        extended_text = "Yet more snow.",
        short_text = "Snow",
        icon_descriptor = "snow",
        rain = Rain(Amount(null, null, "mm"), 0),
        uv = Uv(null, null, null),
        now = null,
      ),
    ),
  )

  @Test
  fun `forecast formats for widget`() {
    val fixedDateTime = ZonedDateTime.of(2022, 7, 1, 19, 30, 0, 0, ZoneId.of("Australia/Melbourne"))

    val output = forecast.formatForWidget(
      strings = FakeStrings(
        R.string.widget_feelsLong to "Feels like %s",
        R.string.widget_today to "Today",
        R.string.widget_tomorrow to "Tomorrow",
      ),
      clock = Clock.fixed(fixedDateTime.toInstant(), fixedDateTime.zone),
    )

    assertThat(output.currentConditions).isEqualTo(
      WidgetCurrentConditions(
        iconRes = R.drawable.ic_weather_snow_24dp,
        location = "Fakezroy",
        description = "Snow",
        feelsLikeText = "Feels like -6°",
        currentTemp = "-5°",
        minTemp = "-7°",
        maxTemp = "3°",
      )
    )

    assertThat(output.hourlyForecasts).containsExactly(
      WidgetHourlyForecast(
        time = "7 PM",
        iconRes = R.drawable.ic_weather_snow_24dp,
        description = null,
        temp = "-5°",
      ),
      WidgetHourlyForecast(
        time = "10 PM",
        iconRes = R.drawable.ic_weather_snow_24dp,
        description = null,
        temp = "-4°",
      ),
    ).inOrder()

    assertThat(output.dateForecasts).containsExactly(
      WidgetDateForecast(
        day = "Today",
        dayShort = "Fri.",
        iconRes = R.drawable.ic_weather_snow_24dp,
        description = "Snow",
        minTemp = "-7°",
        maxTemp = "3°",
      ),
      WidgetDateForecast(
        day = "Tomorrow",
        dayShort = "Sat.",
        iconRes = R.drawable.ic_weather_snow_24dp,
        description = "Snow",
        minTemp = "-6°",
        maxTemp = "4°",
      ),
      WidgetDateForecast(
        day = "Sunday",
        dayShort = "Sun.",
        iconRes = R.drawable.ic_weather_snow_24dp,
        description = "Snow",
        minTemp = "-5°",
        maxTemp = "3°",
      ),
    )
  }
}
