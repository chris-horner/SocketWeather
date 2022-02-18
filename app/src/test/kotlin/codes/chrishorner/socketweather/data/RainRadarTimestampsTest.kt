package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.test.DefaultLocaleRule
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class RainRadarTimestampsTest {

  @get:Rule val localeRule = DefaultLocaleRule(Locale.forLanguageTag("en-AU"))

  @Test fun `timestamps generated from time on the hour`() {
    val timestamps = generateRainRadarTimestamps(getFixedClockAt(12, 0))
    assertThat(timestamps).containsExactly(
      RainTimestamp("202106271100", "11:00am"),
      RainTimestamp("202106271110", "11:10am"),
      RainTimestamp("202106271120", "11:20am"),
      RainTimestamp("202106271130", "11:30am"),
      RainTimestamp("202106271140", "11:40am"),
      RainTimestamp("202106271150", "11:50am"),
    ).inOrder()
  }

  @Test fun `timestamps generated from twelve minutes past the hour`() {
    val timestamps = generateRainRadarTimestamps(getFixedClockAt(0, 12))
    assertThat(timestamps).containsExactly(
      RainTimestamp("202106262310", "11:10pm"),
      RainTimestamp("202106262320", "11:20pm"),
      RainTimestamp("202106262330", "11:30pm"),
      RainTimestamp("202106262340", "11:40pm"),
      RainTimestamp("202106262350", "11:50pm"),
      RainTimestamp("202106270000", "12:00am"),
    ).inOrder()
  }

  @Test fun `timestamps generated from thirty-nine minutes past the hour`() {
    val timestamps = generateRainRadarTimestamps(getFixedClockAt(13, 39))
    assertThat(timestamps).containsExactly(
      RainTimestamp("202106271230", "12:30pm"),
      RainTimestamp("202106271240", "12:40pm"),
      RainTimestamp("202106271250", "12:50pm"),
      RainTimestamp("202106271300", "1:00pm"),
      RainTimestamp("202106271310", "1:10pm"),
      RainTimestamp("202106271320", "1:20pm"),
    ).inOrder()
  }

  private fun getFixedClockAt(hour: Int, minute: Int): Clock {
    val fixedDateTime = LocalDateTime.of(2021, 6, 27, hour, minute)
    val zone = ZoneId.of("UTC")
    val fixedInstant = ZonedDateTime.of(fixedDateTime, zone).toInstant()
    return Clock.fixed(fixedInstant, zone)
  }
}
