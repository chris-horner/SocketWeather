package codes.chrishorner.socketweather.util

import androidx.compose.runtime.Composable
import app.cash.molecule.launchMolecule
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MoleculeScreenModel<Event, State> : ScreenModel {
  @Composable
  fun states(events: Flow<Event>): State
}

@Composable
fun <Event, State> MoleculeScreenModel<Event, State>.stateFlow(events: Flow<Event>): StateFlow<State> {
  return coroutineScope.launchMolecule { states(events) }
}
