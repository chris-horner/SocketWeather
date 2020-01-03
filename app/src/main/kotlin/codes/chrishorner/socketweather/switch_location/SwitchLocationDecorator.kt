package codes.chrishorner.socketweather.switch_location

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.res.getDrawableOrThrow
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import androidx.recyclerview.widget.RecyclerView.State
import kotlin.math.roundToInt

/**
 * Draw a list divider under the first child in the Switch Location list.
 */
class SwitchLocationDecorator(context: Context) : ItemDecoration() {

  private val divider: Drawable

  init {
    val attrArray: TypedArray =
        context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
    divider = attrArray.getDrawableOrThrow(0)
    attrArray.recycle()
  }

  override fun onDraw(c: Canvas, parent: RecyclerView, state: State) {
    val topChild = parent.getChildAt(0)
    val params = topChild.layoutParams as LayoutParams
    val top = topChild.bottom + params.bottomMargin + topChild.translationY.roundToInt()
    val bottom = top + divider.intrinsicHeight
    divider.setBounds(0, top, parent.width, bottom)
    divider.draw(c)
  }
}
