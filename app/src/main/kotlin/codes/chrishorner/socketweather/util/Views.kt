package codes.chrishorner.socketweather.util

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import androidx.annotation.LayoutRes
import androidx.core.view.updatePadding

@Suppress("UNCHECKED_CAST")
fun <T : View> ViewGroup.inflate(@LayoutRes layout: Int, attach: Boolean = false): T =
    LayoutInflater.from(context).inflate(layout, this, attach) as T

fun View.dismissKeyboard() {
  val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imm.hideSoftInputFromWindow(windowToken, 0)
}

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

  doOnApplyWindowInsets { insets, padding ->
    updatePadding(
        left = if (left) padding.left + insets.systemWindowInsetLeft else padding.left,
        top = if (top) padding.top + insets.systemWindowInsetTop else padding.top,
        right = if (right) padding.right + insets.systemWindowInsetRight else padding.right,
        bottom = if (bottom) padding.bottom + insets.systemWindowInsetBottom else padding.bottom
    )
  }
}

private inline fun View.doOnApplyWindowInsets(crossinline block: (insets: WindowInsets, padding: Rect) -> Unit) {
  // Create a snapshot of padding.
  val initialPadding = Rect(paddingLeft, paddingTop, paddingRight, paddingBottom)

  // Set an actual OnApplyWindowInsetsListener which proxies to the given lambda, also passing in the original padding.
  setOnApplyWindowInsetsListener { _, insets ->
    block(insets, initialPadding)
    return@setOnApplyWindowInsetsListener insets
  }

  requestApplyInsetsWhenAttached()
}

private fun View.requestApplyInsetsWhenAttached() {
  if (isAttachedToWindow) {
    // We're already attached, just request as normal.
    requestApplyInsets()
  } else {
    // We're not attached to the hierarchy. Add a listener to request when we are.
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(v: View) {
        v.removeOnAttachStateChangeListener(this)
        v.requestApplyInsets()
      }

      override fun onViewDetachedFromWindow(v: View) = Unit
    })
  }
}
