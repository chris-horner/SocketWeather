package codes.chrishorner.socketweather.test

import androidx.annotation.StringRes
import codes.chrishorner.socketweather.util.Strings
import java.text.DecimalFormat
import java.time.Instant

class FakeStrings(vararg mapping: Pair<Int, String>) : Strings {

  private val decimalFormat = DecimalFormat("0.#")
  private val map = mutableMapOf(*mapping)

  operator fun set(@StringRes key: Int, value: String) {
    map[key] = value
  }

  override fun get(key: Int): String {
    return map.getOrDefault(key, "Default test string")
  }

  override fun get(key: Int, vararg formatArgs: Any?): String {
    return String.format(get(key), *formatArgs)
  }

  override fun getRelativeTimeSpanString(time: Instant): String {
    return "Relative time string"
  }

  override fun formatDegrees(value: Float): String {
    return String.format("%s°", decimalFormat.format(value))
  }

  override fun formatDegrees(value: Int): String {
    return String.format("%s°", decimalFormat.format(value))
  }

  override fun formatPercent(value: Int): String {
    return String.format("%d%%", value)
  }
}
