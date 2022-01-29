package codes.chrishorner.socketweather.data

import kotlinx.coroutines.flow.StateFlow

interface Store<T> {
  val data: StateFlow<T>
  suspend fun set(value: T)
  suspend fun clear()
}
