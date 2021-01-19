package codes.chrishorner.socketweather.data

import app.cash.turbine.FlowTurbine
import com.google.common.truth.Subject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the main coroutines dispatcher for unit testing.
 *
 * See https://medium.com/androiddevelopers/easy-coroutines-in-android-viewmodelscope-25bffb605471
 * and https://github.com/Kotlin/kotlinx.coroutines/tree/master/kotlinx-coroutines-test
 */
class MainDispatcherRule(private val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()) : TestWatcher() {

  override fun starting(description: Description?) {
    super.starting(description)
    Dispatchers.setMain(dispatcher)
  }

  override fun finished(description: Description?) {
    super.finished(description)
    Dispatchers.resetMain()
    dispatcher.cleanupTestCoroutines()
  }
}

class TestCollector<T>(scope: CoroutineScope, flow: Flow<T>) {

  private val collectedValues = mutableListOf<T>()
  private val job = scope.launch { flow.collect { collectedValues.add(it) } }

  operator fun get(index: Int) = collectedValues[index]

  fun dispose() = job.cancel()
}

fun <T> Flow<T>.test(scope: CoroutineScope) = TestCollector(scope, this)

inline fun <reified T> Subject.isInstanceOf() {
  isInstanceOf(T::class.java)
}

/**
 * Like [FlowTurbine.expectItem], except asserts and returns the item as a different type.
 */
suspend inline fun <reified R> FlowTurbine<*>.expectItemAs(): R {
  return expectItem() as? R ?: throw AssertionError("Item isn't expected type ${R::class.simpleName}")
}
