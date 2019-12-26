package codes.chrishorner.socketweather.util

import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener
import com.bluelinelabs.conductor.RouterTransaction
import leakcanary.AppWatcher

fun Controller.asTransaction() = RouterTransaction.with(this)

/**
 * Watch for leaks on [Controller] instances and their views.
 */
object ControllerLeakListener : ControllerChangeListener {

  override fun onChangeStarted(
      to: Controller?,
      from: Controller?,
      isPush: Boolean,
      container: ViewGroup,
      handler: ControllerChangeHandler
  ) {
  }

  override fun onChangeCompleted(
      to: Controller?,
      from: Controller?,
      isPush: Boolean,
      container: ViewGroup,
      handler: ControllerChangeHandler
  ) {
    val objectWatcher = AppWatcher.objectWatcher
    val fromView: View? = from?.view

    if (handler.removesFromViewOnPush() && fromView != null) objectWatcher.watch(fromView)
    if (!isPush && from != null && from.isDestroyed) objectWatcher.watch(from)
  }
}
