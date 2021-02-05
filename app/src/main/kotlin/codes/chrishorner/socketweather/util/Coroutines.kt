package codes.chrishorner.socketweather.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Creates a [Flow] that emits every specified interval of milliseconds.
 */
fun tickerFlow(intervalMs: Long, emitImmediately: Boolean = false): Flow<Unit> = flow {
  if (emitImmediately) emit(Unit)

  while (true) {
    delay(intervalMs)
    emit(Unit)
  }
}
