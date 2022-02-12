package codes.chrishorner.socketweather.data

import app.cash.turbine.FlowTurbine
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runBlockingTest
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.util.Locale
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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

/**
 * Enforce a particular default locale for a test. Resets back to default on completion.
 */
class DefaultLocaleRule(val override: Locale) : TestRule {
  override fun apply(
    base: Statement,
    description: Description
  ): Statement {
    return object : Statement() {
      override fun evaluate() {
        val default = Locale.getDefault()

        try {
          Locale.setDefault(override)
          base.evaluate()
        } finally {
          Locale.setDefault(default)
        }
      }
    }
  }
}
