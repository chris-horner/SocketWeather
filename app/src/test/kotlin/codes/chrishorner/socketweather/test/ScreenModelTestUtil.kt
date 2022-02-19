package codes.chrishorner.socketweather.test

import app.cash.molecule.testing.Event.Error
import app.cash.molecule.testing.MoleculeTurbine
import app.cash.molecule.testing.testMolecule
import codes.chrishorner.socketweather.util.MoleculeScreenModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withTimeout

fun <Event, State> MoleculeScreenModel<Event, State>.test(
  validate: suspend MoleculeScreenModelTester<Event, State>.() -> Unit,
) {
  val events = MutableSharedFlow<Event>(replay = 20)
  testMolecule({ states(events) }) {
    val tester = MoleculeScreenModelTester(this, events)
    tester.validate()
    // TODO: Come up with a better way of asserting no unconsumed events. This _sucks_.
    try {
      val event = withTimeout(1) { awaitEvent() }
      val cause = (event as? Error)?.throwable
      throw AssertionError("Unconsumed event found. $event", cause)
    } catch (ignored: TimeoutCancellationException) {
    }
  }
}

class MoleculeScreenModelTester<Event, State>(
  private val turbine: MoleculeTurbine<State>,
  private val events: MutableSharedFlow<Event>,
) : MoleculeTurbine<State> by turbine {

  private var lastState: State? = null

  override suspend fun awaitItem(): State {
    // De-dupe emissions to make testing simpler and match behaviour of StateFlow.
    while (true) {
      val nextState = turbine.awaitItem()
      if (nextState != lastState) {
        lastState = nextState
        return nextState
      }
    }
  }

  fun sendEvent(event: Event) {
    if (!events.tryEmit(event)) {
      throw AssertionError("Unable to send event.")
    }
  }
}
