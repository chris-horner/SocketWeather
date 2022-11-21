package codes.chrishorner.socketweather.util.navigation

import android.content.Context
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import codes.chrishorner.socketweather.Navigator
import codes.chrishorner.socketweather.Presenter
import codes.chrishorner.socketweather.Screen
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.plus
import kotlinx.parcelize.Parcelize
import cafe.adriel.voyager.core.screen.Screen as VoyagerScreen
import cafe.adriel.voyager.navigator.Navigator as VoyagerNavigator

/**
 * Takes in an initial [Screen] and uses Voyager as a [Navigator] implementation to
 * push and pop destinations.
 */
@Composable
fun VoyagerNavigation(initialScreen: Screen<*, *>) {
  VoyagerNavigator(initialScreen.toVoyagerScreen()) { navigator -> FadeThroughTransition(navigator) }
}

/**
 * Adapts Voyager's navigator to conform to [Navigator].
 */
@Composable
private fun VoyagerNavigator.rememberAsNavigator(): Navigator {
  return remember(this) { DelegatingNavigator(this) }
}

/**
 * Adapts a [Screen] to Voyager's concept of a screen.
 */
private fun <Event, State> Screen<Event, State>.toVoyagerScreen(): VoyagerScreen {
  return DelegatingVoyagerScreen(this)
}

private class DelegatingNavigator(private val voyagerNavigator: VoyagerNavigator) : Navigator {
  override val canPop: Boolean
    get() = voyagerNavigator.canPop

  override fun push(screen: Screen<*, *>) {
    voyagerNavigator.push(screen.toVoyagerScreen())
  }

  override fun pop() {
    voyagerNavigator.pop()
  }

  override fun replaceAllWith(screen: Screen<*, *>) {
    voyagerNavigator.replaceAll(screen.toVoyagerScreen())
  }
}

@Parcelize
private data class DelegatingVoyagerScreen<Event, State>(
  private val screen: Screen<Event, State>,
) : VoyagerScreen, Parcelable {
  @Composable
  override fun Content() {
    val context: Context = LocalContext.current
    val voyagerNavigator = LocalNavigator.currentOrThrow
    val navigator = voyagerNavigator.rememberAsNavigator()
    val screenModel = rememberScreenModel { MoleculeScreenModel(screen.onCreatePresenter(context, navigator)) }
    val state by screenModel.states.collectAsState()
    screen.Content(state) { event -> screenModel.events.tryEmit(event) }
  }

  override val key: ScreenKey
    get() = screen::class.qualifiedName!!
}

/**
 * Similar to Voyager's [StateScreenModel], except using Molecule to handle state generation.
 */
private class MoleculeScreenModel<Event, State>(presenter: Presenter<Event, State>) : ScreenModel {
  val events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
  val states = moleculeScope.launchMolecule(RecompositionClock.Immediate) { presenter.states(events) }
}

private val ScreenModel.moleculeScope: CoroutineScope
  get() = ScreenModelStore.getOrPutDependency(
    screenModel = this,
    name = "ScreenModelMoleculeScope",
    factory = { key -> CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) + CoroutineName(key) },
    onDispose = { scope -> scope.cancel() }
  )
