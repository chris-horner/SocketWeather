package codes.chrishorner.socketweather.util

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.ViewFlipper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.forEachIndexed
import androidx.core.view.updatePadding

@Suppress("UNCHECKED_CAST")
fun <T : View> ViewGroup.inflate(@LayoutRes layout: Int, attach: Boolean = false): T =
    LayoutInflater.from(context).inflate(layout, this, attach) as T

fun View.dismissKeyboard() {
  val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.dpToPx(dp: Int): Int = dpToPx(dp.toFloat()).toInt()

fun View.dpToPx(dp: Float): Float = dp * resources.displayMetrics.density

/**
 * Indicates that this view should update its own padding to match that of [WindowInsets].
 *
 * This is useful for views that display themselves under the status bar or navigation bar,
 * allowing content to be displayed edge-to-edge.
 */
fun View.updatePaddingWithInsets(
    left: Boolean = false,
    top: Boolean = false,
    right: Boolean = false,
    bottom: Boolean = false
) {
  // Create a snapshot of padding.
  val initialPadding = Rect(paddingLeft, paddingTop, paddingRight, paddingBottom)

  doOnApplyWindowInsets { insets ->
    val systemBarInsets = insets.getInsets(Type.systemBars())
    updatePadding(
        left = if (left) initialPadding.left + systemBarInsets.left else initialPadding.left,
        top = if (top) initialPadding.top + systemBarInsets.top else initialPadding.top,
        right = if (right) initialPadding.right + systemBarInsets.right else initialPadding.right,
        bottom = if (bottom) initialPadding.bottom + systemBarInsets.bottom else initialPadding.bottom
    )
  }
}

inline fun View.doOnApplyWindowInsets(crossinline block: (insets: WindowInsetsCompat) -> Unit) {

  // Set an actual OnApplyWindowInsetsListener which proxies to the given lambda.
  ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
    block(insets)
    return@setOnApplyWindowInsetsListener insets
  }

  if (isAttachedToWindow) {
    // We're already attached, just request as normal.
    ViewCompat.requestApplyInsets(this)
  } else {
    // We're not attached to the hierarchy. Add a listener to request when we are.
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(v: View) {
        v.removeOnAttachStateChangeListener(this)
        ViewCompat.requestApplyInsets(v)
      }

      override fun onViewDetachedFromWindow(v: View) = Unit
    })
  }
}

fun ViewFlipper.setDisplayedChildId(@IdRes id: Int) {
  forEachIndexed { index, view ->
    if (view.id == id) {
      if (displayedChild == index) return
      displayedChild = index
      return
    }
  }

  val name = resources.getResourceName(id)
  throw IllegalArgumentException("No child with ID $name")
}
