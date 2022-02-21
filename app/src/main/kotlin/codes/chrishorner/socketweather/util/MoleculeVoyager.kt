package codes.chrishorner.socketweather.util

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import app.cash.molecule.launchMolecule
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

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
    val events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    val state by screenModel.stateFlow(events).collectAsState()
    Content(state) { event -> events.tryEmit(event) }
  }
}

interface MoleculeScreenModel<Event, State> : ScreenModel {
  @Composable
  fun states(events: Flow<Event>): State
}

@Composable
private fun <Event, State> MoleculeScreenModel<Event, State>.stateFlow(events: Flow<Event>): StateFlow<State> {
  return coroutineScope.launchMolecule { states(events) }
}
