package codes.chrishorner.socketweather.rain_radar

import cafe.adriel.voyager.core.stack.StackEvent
import codes.chrishorner.socketweather.data.generateRainRadarTimestamps
import codes.chrishorner.socketweather.home.HomeScreen
import codes.chrishorner.socketweather.test.DefaultLocaleRule
import codes.chrishorner.socketweather.test.FakeNavigator
import codes.chrishorner.socketweather.test.test
import codes.chrishorner.socketweather.test.testWithScheduler
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class RainRadarScreenModelTest {

  @get:Rule val localeRule = DefaultLocaleRule(Locale.forLanguageTag("en-AU"))

  private val navigator = FakeNavigator(HomeScreen, RainRadarScreen)
  private val zone: ZoneId = ZoneId.of("Australia/Melbourne")
  private val location = RainRadarLocation(
    latitude = -37.80052185058594,
    longitude = 144.97901916503906,
    timezone = zone,
    zoom = 8.0,
  )
  private val clock: Clock

  init {
    val fixedDateTime = LocalDateTime.of(2021, 8, 29, 16, 0)
    val fixedInstant = ZonedDateTime.of(fixedDateTime, zone).toInstant()
    clock = Clock.fixed(fixedInstant, zone)
  }

  @Test fun `active timestamp index and subtitle update over time`() = runTest {
    generateRainRadarStates(location, generateRainRadarTimestamps(clock)).testWithScheduler {
      with(awaitItem()) {
        assertThat(subtitle).isEqualTo("3:00pm")
        assertThat(activeTimestampIndex).isEqualTo(0)
      }

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

  @Test fun `first state emission`() {
    // Generated timestamps are in UTC.
    val expectedTimestamps = listOf(
      "202108290500",
      "202108290510",
      "202108290520",
      "202108290530",
      "202108290540",
      "202108290550",
    )

    RainRadarScreenModel(navigator, location, clock).test {
      assertThat(awaitItem()).isEqualTo(
        RainRadarState(
          location = location,
          subtitle = "3:00pm",
          timestamps = expectedTimestamps,
          activeTimestampIndex = 0
        )
      )
    }
  }

  @Test fun `back press navigates back`() {
    RainRadarScreenModel(navigator, location, clock).test {
      awaitItem()
      sendEvent(RainRadarBackPressEvent)
      with(navigator.awaitChange()) {
        assertThat(event).isEqualTo(StackEvent.Pop)
        assertThat(items).containsExactly(HomeScreen)
      }
    }
  }
}
