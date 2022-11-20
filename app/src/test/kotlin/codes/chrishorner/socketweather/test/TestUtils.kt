package codes.chrishorner.socketweather.test

import com.google.common.truth.IterableSubject
import com.google.common.truth.Subject

inline fun <reified T> Subject.isInstanceOf() {
  isInstanceOf(T::class.java)
}

fun <T> IterableSubject.containsExactlyInOrder(vararg items: T) {
  containsExactly(*items).inOrder()
}
