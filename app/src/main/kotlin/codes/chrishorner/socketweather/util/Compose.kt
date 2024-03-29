package codes.chrishorner.socketweather.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow

@Composable
inline fun <T> CollectEffect(flow: Flow<T>, crossinline block: CoroutineScope.(T) -> Unit) {
  LaunchedEffect(flow) {
    coroutineScope {
      flow.collect { block(it) }
    }
  }
}

fun <T> MutableState<T>.update(block: (T) -> T) {
  value = block(value)
}
