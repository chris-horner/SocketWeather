package codes.chrishorner.socketweather.util

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.ControllerChangeHandler.ControllerChangeListener
import com.bluelinelabs.conductor.RouterTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import leakcanary.AppWatcher

fun Controller.asTransaction() = RouterTransaction.with(this)

abstract class ScopedController<VM : Any, P : Any>(args: Bundle? = null) : Controller(args) {

  private var viewScope: CoroutineScope? = null
  private var presenter: P? = null
  private var viewModel: VM? = null

  final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
    val view = onCreateView(container)
    val vm: VM = viewModel ?: onCreateViewModel(view.context.applicationContext).also { viewModel = it }
    presenter = onCreatePresenter(view, vm)
    return view
  }

  final override fun onAttach(view: View) {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    viewScope = scope
    val presenterToAttach = requireNotNull(presenter) { "presenter shouldn't be null in onAttach()." }
    val vmToAttach = requireNotNull(viewModel) { "viewModel shouldn't be null in onAttach()." }
    onAttach(view, presenterToAttach, vmToAttach, scope)
  }

  final override fun onDetach(view: View) {
    val scope = requireNotNull(viewScope) { "viewScope shouldn't be null in onDetach()." }
    val presenterToDetach = requireNotNull(presenter) { "presenter shouldn't be null in onDetach()." }
    val vmToDetach = requireNotNull(viewModel) { "viewModel shouldn't be null in onDetach()." }
    onDetach(view, presenterToDetach, vmToDetach, scope)
    scope.cancel()
    viewScope = null
  }

  final override fun onDestroyView(view: View) {
    val currentPresenter = requireNotNull(presenter) { "presenter shouldn't be null in onDestroyView()." }
    onDestroyView(view, currentPresenter)
    presenter = null
  }

  final override fun onDestroy() {
    onDestroy(viewModel)
    viewModel = null
  }

  protected fun getPresenter(): P? = presenter
  protected fun getViewModel(): VM? = viewModel

  abstract fun onCreateView(container: ViewGroup): View
  abstract fun onCreatePresenter(view: View, viewModel: VM): P
  abstract fun onCreateViewModel(context: Context): VM
  open fun onAttach(view: View, presenter: P, viewModel: VM, viewScope: CoroutineScope) {}
  open fun onDetach(view: View, presenter: P, viewModel: VM, viewScope: CoroutineScope) {}
  open fun onDestroyView(view: View, presenter: P) {}
  open fun onDestroy(viewModel: VM?) {}
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

    if (handler.removesFromViewOnPush() && fromView != null) {
      objectWatcher.watch(fromView, "View should be cleared when navigated away from")
    }

    if (!isPush && from != null && from.isDestroyed) {
      objectWatcher.watch(from, "Controller should be destroyed and cleared")
    }
  }
}
