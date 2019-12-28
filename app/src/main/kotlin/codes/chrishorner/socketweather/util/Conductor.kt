package codes.chrishorner.socketweather.util

import android.os.Bundle
import android.view.LayoutInflater
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

abstract class ScopedController<P : Any>(args: Bundle? = null) : Controller(args) {

  private var viewScope: CoroutineScope? = null
  private var presenter: P? = null

  final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
    val view = onCreateView(container)
    presenter = onCreatePresenter(view)
    return view
  }

  final override fun onAttach(view: View) {
    val scope = MainScope()
    viewScope = scope
    val presenterToAttach = requireNotNull(presenter) { "presenter shouldn't be null in onAttach()." }
    onAttach(view, presenterToAttach, scope)
  }

  final override fun onDetach(view: View) {
    val scope = requireNotNull(viewScope) { "viewScope shouldn't be null in onDetach()." }
    val presenterToDetach = requireNotNull(presenter) { "presenter shouldn't be null in onDetach()." }
    onDetach(view, presenterToDetach, scope)
    scope.cancel()
  }

  override fun onDestroyView(view: View) {
    val currentPresenter = requireNotNull(presenter) { "presenter shouldn't be null in onDestroyView()." }
    onDestroyView(view, currentPresenter)
    presenter = null
  }

  protected fun getPresenter(): P? = presenter

  abstract fun onCreateView(container: ViewGroup): View
  abstract fun onCreatePresenter(view: View): P
  open fun onAttach(view: View, presenter: P, viewScope: CoroutineScope) {}
  open fun onDetach(view: View, presenter: P, viewScope: CoroutineScope) {}
  open fun onDestroyView(view: View, presenter: P) {}
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
