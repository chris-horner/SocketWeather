package codes.chrishorner.socketweather.test

import app.cash.turbine.FlowTurbine
import app.cash.turbine.test
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runBlockingTest
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * The same as [runBlockingTest], except the [TestCoroutineScope] is cancelled at the end of
 * `testBody`'s execution, along with any coroutines that are still running.
 *
 * The key difference is that `runBlockingTest` throws an exception if it finishes
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

suspend fun <T> Flow<T>.testWithScheduler(validate: suspend FlowTurbine<T>.() -> Unit) {
  val testScheduler = coroutineContext[TestCoroutineScheduler]
    ?: error("testWithScheduler must be run inside runTest {}.")
  flowOn(UnconfinedTestDispatcher(testScheduler)).test(validate = validate)
}

inline fun <reified T> Subject.isInstanceOf() {
  isInstanceOf(T::class.java)
}

@OptIn(ExperimentalContracts::class)
inline fun <reified T> Any?.assertIsOfType() {
  contract {
    returns() implies (this@assertIsOfType is T)
  }
  assertThat(this).isInstanceOf<T>()
}

/**
 * Like [FlowTurbine.awaitItem], except asserts and returns the item as a different type.
 */
suspend inline fun <reified R> FlowTurbine<*>.awaitItemAs(): R {
  return awaitItem() as? R ?: throw AssertionError("Item isn't expected type ${R::class.simpleName}")
}
