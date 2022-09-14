package codes.chrishorner.socketweather.test

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import codes.chrishorner.socketweather.util.MoleculeScreenModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withTimeout
import app.cash.turbine.Event as TurbineEvent

suspend fun <Event, State> MoleculeScreenModel<Event, State>.test(
  validate: suspend MoleculeScreenModelTester<Event, State>.() -> Unit,
) {
  val events = MutableSharedFlow<Event>(replay = 20)
  moleculeFlow(clock = RecompositionClock.Immediate) { states(events) }.test {
    val tester = MoleculeScreenModelTester(this, events)
    tester.validate()
    // TODO: Come up with a better way of asserting no unconsumed events. This _sucks_.
    try {
      val event = withTimeout(1) { awaitEvent() }
      val cause = (event as? TurbineEvent.Error)?.throwable
      throw AssertionError("Unconsumed event found. $event", cause)
    } catch (ignored: TimeoutCancellationException) {
    }
  }
}

class MoleculeScreenModelTester<Event, State>(
  private val turbine: ReceiveTurbine<State>,
  private val events: MutableSharedFlow<Event>,
) : ReceiveTurbine<State> by turbine {

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
