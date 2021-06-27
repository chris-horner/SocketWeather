package codes.chrishorner.socketweather.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

class RainRadarTimestampsTest {

  @Test fun `timestamps generated from time on the hour`() {
    val timestamps = generateRainRadarTimestamps(getFixedClockAt(12, 0))
    assertThat(timestamps).containsExactly(
      "202106270100",
      "202106270110",
      "202106270120",
      "202106270130",
      "202106270140",
      "202106270150"
    )
  }

  @Test fun `timestamps generated from twelve minutes past the hour`() {
    val timestamps = generateRainRadarTimestamps(getFixedClockAt(15, 12))
    assertThat(timestamps).containsExactly(
      "202106270410",
      "202106270420",
      "202106270430",
      "202106270440",
      "202106270450",
      "202106270500"
    )
  }

  @Test fun `timestamps generated from thirty-nine minutes past the hour`() {
    val timestamps = generateRainRadarTimestamps(getFixedClockAt(13, 39))
    assertThat(timestamps).containsExactly(
      "202106270230",
      "202106270240",
      "202106270250",
      "202106270300",
      "202106270310",
      "202106270320"
    )
  }

  private fun getFixedClockAt(hour: Int, minute: Int): Clock {
    val fixedDateTime = LocalDateTime.of(2021, 6, 27, hour, minute)
    val zone = ZoneId.of("Australia/Melbourne")
    val fixedInstant = ZonedDateTime.of(fixedDateTime, zone).toInstant()
    return Clock.fixed(fixedInstant, zone)
  }
}
