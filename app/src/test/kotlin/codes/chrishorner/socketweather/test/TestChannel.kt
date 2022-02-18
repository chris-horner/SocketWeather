package codes.chrishorner.socketweather.test

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.withTimeout

class TestChannel<T> {
  private val channel = Channel<T>(capacity = UNLIMITED)

  suspend fun awaitValue() = try {
    withTimeout(1_000L) { channel.receive() }
  } catch (e: TimeoutCancellationException) {
    error("No value produced in 1,000ms.")
  }

  fun send(value: T) = channel.trySend(value)
}
