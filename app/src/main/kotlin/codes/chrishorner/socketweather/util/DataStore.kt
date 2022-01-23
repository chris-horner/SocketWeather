package codes.chrishorner.socketweather.util

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking

/**
 * Converts a [DataStore] to [StateFlow] using a synchronous disk read.
 * `started` strategy is [SharingStarted.Eagerly] by default, meaning the subscription never ends.
 */
fun <T> DataStore<T>.blockingAsStateFlow(
  scope: CoroutineScope,
  started: SharingStarted = SharingStarted.Eagerly
): StateFlow<T> {
  val initialValue = runBlocking { data.first() }
  return data.stateIn(scope, started, initialValue)
}
