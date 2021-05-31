package codes.chrishorner.socketweather.util

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import codes.chrishorner.socketweather.R
import org.threeten.bp.Instant
import java.text.DecimalFormat

interface Strings {

  operator fun get(@StringRes key: Int): String
  fun get(@StringRes key: Int, vararg formatArgs: Any?): String
  fun getRelativeTimeSpanString(time: Instant): String
  fun formatDegrees(value: Float): String
  fun formatDegrees(value: Int): String
  fun formatPercent(value: Int): String

  class AndroidStrings(context: Context) : Strings {

    private val res = context.applicationContext.resources
    private val decimalFormat = DecimalFormat("0.#")

    override fun get(key: Int): String = res.getString(key)

    override fun get(key: Int, vararg formatArgs: Any?): String {
      return res.getString(key, *formatArgs)
    }

    override fun getRelativeTimeSpanString(time: Instant): String {
      return DateUtils.getRelativeTimeSpanString(time.toEpochMilli()).toString()
    }

    override fun formatDegrees(value: Float): String {
      return res.getString(R.string.temperatureFormat, decimalFormat.format(value))
    }

    override fun formatDegrees(value: Int): String {
      return res.getString(R.string.temperatureFormat, decimalFormat.format(value))
    }

    override fun formatPercent(value: Int): String {
      return res.getString(R.string.percentFormat, value)
    }
  }
}