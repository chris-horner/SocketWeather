package codes.chrishorner.socketweather.data

import kotlinx.coroutines.flow.StateFlow

interface Store<T> {
  val data: StateFlow<T>
  suspend fun set(value: T)
  suspend fun clear()
}

suspend fun <T> Store<T>.update(block: (current: T) -> T) {
  set(block(data.value))
}
