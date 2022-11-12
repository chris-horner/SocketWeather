package codes.chrishorner.socketweather.test

import app.cash.molecule.RecompositionClock
import app.cash.molecule.moleculeFlow
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import codes.chrishorner.socketweather.Presenter
import kotlinx.coroutines.flow.MutableSharedFlow

suspend fun <Event, State> Presenter<Event, State>.test(
  validate: suspend PresenterTester<Event, State>.() -> Unit,
) {
  val events = MutableSharedFlow<Event>(replay = 20)
  moleculeFlow(clock = RecompositionClock.Immediate) { states(events) }.test {
    val tester = PresenterTester(this, events)
    tester.validate()
    tester.ensureAllEventsConsumed()
  }
}

class PresenterTester<Event, State>(
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
