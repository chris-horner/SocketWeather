package codes.chrishorner.socketweather.test

import codes.chrishorner.socketweather.data.Store
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeStore<T>(val default: T) : Store<T> {

  val mutableDataFlow = MutableStateFlow(default)

  override val data: StateFlow<T> = mutableDataFlow

  override suspend fun set(value: T) {
    mutableDataFlow.value = value
  }

  override suspend fun clear() {
    mutableDataFlow.value = default
  }
}
