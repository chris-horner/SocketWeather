package codes.chrishorner.socketweather.test

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Subject
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.coroutineContext

suspend fun <T> Flow<T>.testWithScheduler(validate: suspend ReceiveTurbine<T>.() -> Unit) {
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
