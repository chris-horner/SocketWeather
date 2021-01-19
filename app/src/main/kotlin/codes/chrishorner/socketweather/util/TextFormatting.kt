package codes.chrishorner.socketweather.util

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getResourceIdOrThrow
import codes.chrishorner.socketweather.R
import java.text.DecimalFormat

private val decimalFormat = DecimalFormat("0.#")

fun Float.formatAsDegrees(context: Context): String {
  return context.getString(R.string.temperatureFormat, decimalFormat.format(this))
}

fun Int.formatAsDegrees(context: Context): String {
  return context.getString(R.string.temperatureFormat, decimalFormat.format(this))
}

fun Int.formatAsPercent(context: Context): String {
  return context.getString(R.string.percentFormat, this)
}

fun TypedArray.getCompatFontOrThrow(context: Context, index: Int): Typeface {
  if (!hasValue(index)) throw IllegalArgumentException("Attribute not defined in set.")
  val fontId = getResourceIdOrThrow(index)
  return ResourcesCompat.getFont(context, fontId)!!
}
