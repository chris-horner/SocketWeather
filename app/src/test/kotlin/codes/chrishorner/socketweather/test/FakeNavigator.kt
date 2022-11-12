package codes.chrishorner.socketweather.test

import app.cash.turbine.Turbine
import codes.chrishorner.socketweather.Navigator
import codes.chrishorner.socketweather.Screen
import kotlinx.coroutines.yield

class FakeNavigator(vararg initialScreens: Screen<*, *>) : Navigator {

  private val stack = mutableListOf<Screen<*, *>>()
  private val changes = Turbine<List<Screen<*, *>>>()

  init {
    stack.addAll(initialScreens)
  }

  suspend fun awaitStackChange(): List<Screen<*, *>> {
    return changes.awaitItem()
  }

  suspend fun assertNoChanges() {
    yield() // Give other coroutines a chance to finish.
    changes.ensureAllEventsConsumed()
  }

  fun setStack(vararg screens: Screen<*, *>) {
    stack.clear()
    stack.addAll(screens)
  }

  override val canPop: Boolean
    get() = stack.size > 1

  override fun push(screen: Screen<*, *>) {
    stack += screen
    changes.add(stack.toList())
  }

  override fun pop() {
    if (!canPop) return
    stack.removeLast()
    changes.add(stack.toList())
  }

  override fun replaceAllWith(screen: Screen<*, *>) {
    stack.clear()
    stack += screen
    changes.add(stack.toList())
  }
}
