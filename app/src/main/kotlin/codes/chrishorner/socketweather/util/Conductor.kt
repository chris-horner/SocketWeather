package codes.chrishorner.socketweather.util

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import leakcanary.AppWatcher

fun Controller.asTransaction() = RouterTransaction.with(this)

abstract class ScopedController(args: Bundle? = null) : Controller(args) {

  private var viewScope: CoroutineScope? = null

  final override fun onAttach(view: View) {
    val scope = MainScope()
    viewScope = scope
    onAttach(view, MainScope())
  }

  final override fun onDetach(view: View) {
    val scope = requireNotNull(viewScope) { "viewScope shouldn't be null in onDetach()." }
    onDetach(view, scope)
    scope.cancel()
  }

  open fun onAttach(view: View, viewScope: CoroutineScope) {}
  open fun onDetach(view: View, viewScope: CoroutineScope) {}
}

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
