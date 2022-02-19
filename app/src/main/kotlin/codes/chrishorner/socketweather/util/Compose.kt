package codes.chrishorner.socketweather.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Like
 * ```
 * LaunchedEffect(flow) {
 *   flow.collect { }
 * }
 * ```
 * but forces the collecting code to handle emissions without suspending.
 */
@Composable
inline fun <T> CollectEffect(flow: Flow<T>, crossinline block: CoroutineScope.(T) -> Unit) {
  LaunchedEffect(flow) {
    coroutineScope {
      flow.collect { block(it) }
    }
  }
}
