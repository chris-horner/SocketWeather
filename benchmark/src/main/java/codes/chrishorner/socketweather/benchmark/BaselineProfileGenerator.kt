package codes.chrishorner.socketweather.benchmark

import androidx.benchmark.macro.ExperimentalBaselineProfilesApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

@ExperimentalBaselineProfilesApi
class BaselineProfileGenerator {
  @get:Rule
  val baselineProfileRule = BaselineProfileRule()

  @Test
  fun generate() = baselineProfileRule.collectBaselineProfile("codes.chrishorner.socketweather.benchmark") {
    pressHome()
    startActivityAndWait { intent ->
      intent.putExtra("open_at_location_picker", true)
    }

    // Search for Melbourne and choose it as a location.
    device.wait(Until.hasObject(By.res("search_input")), 5_000)
    val searchBox = device.findObject(By.res("search_input"))
    searchBox.click()
    device.waitForIdle()
    searchBox.text = "Melbourne"
    device.wait(Until.hasObject(By.res("search_result_postcode:3000")), 5_000) // Melbourne's postcode
    device.findObject(By.res("search_result_postcode:3000")).click()

    // Wait for forecast to load, then scroll around.
    device.wait(Until.hasObject(By.res("forecast_scroll_container")), 5_000)
    device.findObject(By.res("more_button")).click()
    val scrollContainer = device.findObject(By.res("forecast_scroll_container"))
    scrollContainer.fling(Direction.DOWN)
    device.waitForIdle()
    scrollContainer.fling(Direction.UP)
    device.waitForIdle()
  }
}
