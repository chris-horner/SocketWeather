package codes.chrishorner.socketweather.rain_radar

import app.cash.turbine.test
import codes.chrishorner.socketweather.data.DefaultLocaleRule
import codes.chrishorner.socketweather.data.runCancellingBlockingTest
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class RainRadarViewModelTest {

  @get:Rule val localeRule = DefaultLocaleRule(Locale.forLanguageTag("en-AU"))

  private val zone: ZoneId = ZoneId.of("Australia/Melbourne")
  private val clock: Clock

  init {
    val fixedDateTime = LocalDateTime.of(2021, 8, 29, 16, 0)
    val fixedInstant = ZonedDateTime.of(fixedDateTime, zone).toInstant()
    clock = Clock.fixed(fixedInstant, zone)
  }

  @Test fun `active timestamp index and subtitle update over time`() = runCancellingBlockingTest {

    val location = RainRadarLocation(
      latitude = -37.80052185058594,
      longitude = 144.97901916503906,
      timezone = zone,
      zoom = 8.0,
    )

    val viewModel = RainRadarViewModel(location, clock, overrideScope = this)

    // Generated timestamps are in UTC.
    val expectedTimestamps = listOf(
      "202108290500",
      "202108290510",
      "202108290520",
      "202108290530",
      "202108290540",
      "202108290550",
    )

    val expectedState = RainRadarState(
      location = location,
      subtitle = "3:00pm",
      timestamps = expectedTimestamps,
      activeTimestampIndex = 0
    )

    viewModel.states.test {
      assertThat(awaitItem()).isEqualTo(expectedState)

      advanceTimeBy(500)

      with(awaitItem()) {
        assertThat(subtitle).isEqualTo("3:10pm")
        assertThat(activeTimestampIndex).isEqualTo(1)
      }

      advanceTimeBy(500)

      with(awaitItem()) {
        assertThat(subtitle).isEqualTo("3:20pm")
        assertThat(activeTimestampIndex).isEqualTo(2)
      }

      advanceTimeBy(500)

      with(awaitItem()) {
        assertThat(subtitle).isEqualTo("3:30pm")
        assertThat(activeTimestampIndex).isEqualTo(3)
      }

      advanceTimeBy(500)

      with(awaitItem()) {
        assertThat(subtitle).isEqualTo("3:40pm")
        assertThat(activeTimestampIndex).isEqualTo(4)
      }

      advanceTimeBy(500)

      with(awaitItem()) {
        assertThat(subtitle).isEqualTo("3:50pm")
        assertThat(activeTimestampIndex).isEqualTo(5)
      }

      advanceTimeBy(1_000)

      with(awaitItem()) {
        assertThat(subtitle).isEqualTo("3:00pm")
        assertThat(activeTimestampIndex).isEqualTo(0)
      }
    }
  }
}
