package codes.chrishorner.socketweather.util

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources

val Context.app: Application
  get() = applicationContext as Application

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

fun Context.requireDrawable(@DrawableRes resId: Int): Drawable {
  return requireNotNull(AppCompatResources.getDrawable(this, resId)) {
    "Required Drawable is null."
  }
}
