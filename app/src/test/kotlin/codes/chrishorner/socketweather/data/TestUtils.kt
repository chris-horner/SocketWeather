package codes.chrishorner.socketweather.data

import app.cash.turbine.FlowTurbine
import com.google.common.truth.Subject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.UncompletedCoroutinesError
import kotlinx.coroutines.test.runBlockingTest
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The same as [runBlockingTest], except the [TestCoroutineScope] is cancelled at the end of
 * `testBody`'s execution, along with any coroutines that are still running.
 *
 * The key difference is that `runBlockingTest` throws [UncompletedCoroutinesError] if it finishes
 * with coroutines running, where as this function simply cancels them.
 *
 * This is useful when testing code that subscribes to unending streams (such as a [SharedFlow]),
 * and rather than orchestrating cancellation from that source manually you simply want the end
 * of the test to cancel that subscription.
 *
 * @see runBlockingTest
 */
fun runCancellingBlockingTest(
  context: CoroutineContext = EmptyCoroutineContext,
  testBody: suspend TestCoroutineScope.() -> Unit
) = runBlockingTest(context) {
  val job = Job()
  val scope = TestCoroutineScope(coroutineContext + job)
  testBody(scope)
  scope.advanceUntilIdle()
  job.cancel()
}

inline fun <reified T> Subject.isInstanceOf() {
  isInstanceOf(T::class.java)
}

/**
 * Like [FlowTurbine.expectItem], except asserts and returns the item as a different type.
 */
suspend inline fun <reified R> FlowTurbine<*>.expectItemAs(): R {
  return awaitItem() as? R ?: throw AssertionError("Item isn't expected type ${R::class.simpleName}")
}
