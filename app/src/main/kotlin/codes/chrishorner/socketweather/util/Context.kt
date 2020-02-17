package codes.chrishorner.socketweather.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes

@StyleRes fun Context.resolveAttr(@AttrRes attrId: Int): Int {
  val typedValue = TypedValue()
  theme.resolveAttribute(attrId, typedValue, true)
  return typedValue.resourceId
}

@ColorInt fun Context.getThemeColour(@AttrRes attrId: Int): Int {
  val typedValue = TypedValue()
  theme.resolveAttribute(attrId, typedValue, true)
  val colourRes: Int =
      if (typedValue.resourceId != 0) typedValue.resourceId
      else typedValue.data

  return getColor(colourRes)
}
