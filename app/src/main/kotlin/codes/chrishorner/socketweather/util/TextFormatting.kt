package codes.chrishorner.socketweather.util

import android.content.Context
import codes.chrishorner.socketweather.R
import java.text.DecimalFormat

private val decimalFormat = DecimalFormat("0.#")

fun Float.formatAsDegrees(context: Context): String {
  return context.getString(R.string.temperatureFormat, decimalFormat.format(this))
}

fun Int.formatAsDegrees(context: Context): String {
  return context.getString(R.string.temperatureFormat, decimalFormat.format(this))
}
