package codes.chrishorner.socketweather.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.ScreenModelStore
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.plus
import kotlin.LazyThreadSafetyMode.NONE

typealias Navigator = Stack<Screen>

abstract class MoleculeScreen<Event, State> : Screen {

  abstract fun onCreateScreenModel(context: Context, navigator: Navigator): MoleculeScreenModel<Event, State>

  @Composable
  abstract fun Content(state: State, onEvent: (Event) -> Unit)

  @Composable
  final override fun Content() {
    val context = LocalContext.current
    val navigator = LocalNavigator.currentOrThrow
    val screenModel = rememberScreenModel { onCreateScreenModel(context, navigator) }
    val state by screenModel.states.collectAsState()
    Content(state) { event -> screenModel.events.tryEmit(event) }
  }
}

abstract class MoleculeScreenModel<Event, State> : ScreenModel {

  val events by lazy(NONE) { MutableSharedFlow<Event>(extraBufferCapacity = 1) }
  val states by lazy(NONE) {
    moleculeScope.launchMolecule(RecompositionClock.Immediate) { states(events) }
  }

  @Composable
  abstract fun states(events: Flow<Event>): State
}

private val ScreenModel.moleculeScope: CoroutineScope
  get() = ScreenModelStore.getOrPutDependency(
    screenModel = this,
    name = "ScreenModelMoleculeScope",
    factory = { key -> CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate) + CoroutineName(key) },
    onDispose = { scope -> scope.cancel() }
  )
