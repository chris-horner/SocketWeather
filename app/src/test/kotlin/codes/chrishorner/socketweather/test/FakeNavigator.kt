package codes.chrishorner.socketweather.test

import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import codes.chrishorner.socketweather.util.Navigator
import kotlinx.coroutines.yield

class FakeNavigator : Navigator {

  data class Change(val event: StackEvent, val items: List<Screen>)

  private val changes = TestChannel<Change>()

  suspend fun awaitChange(): Change {
    return changes.awaitValue()
  }

  suspend fun assertNoChanges() {
    yield() // Give other coroutines a chance to finish.
    changes.assertEmpty()
  }

  override val items: MutableList<Screen> = mutableListOf()

  override var lastEvent: StackEvent = StackEvent.Idle
    private set

  override val canPop: Boolean
    get() = items.isNotEmpty()

  override val isEmpty: Boolean
    get() = items.isEmpty()

  override val lastItemOrNull: Screen?
    get() = items.lastOrNull()

  override val lastOrNull: Screen?
    get() = items.lastOrNull()

  override val size: Int
    get() = items.size

  override fun clearEvent() {
    lastEvent = StackEvent.Idle
  }

  override fun plusAssign(item: Screen) {
    push(item)
  }

  override fun plusAssign(items: List<Screen>) {
    push(items)
  }

  override fun pop(): Boolean {
    return if (canPop) {
      items.removeLast()
      lastEvent = StackEvent.Pop
      changes.send(Change(lastEvent, items.toList()))
      true
    } else {
      false
    }
  }

  override fun popAll() {
    popUntil { false }
  }

  override infix fun popUntil(predicate: (Screen) -> Boolean): Boolean {
    var success = false
    val shouldPop = {
      lastItemOrNull
        ?.let(predicate)
        ?.also { success = it }
        ?.not()
        ?: false
    }

    while (canPop && shouldPop()) {
      items.removeLast()
    }

    lastEvent = StackEvent.Pop
    changes.send(Change(lastEvent, items.toList()))

    return success
  }

  override infix fun push(item: Screen) {
    items += item
    lastEvent = StackEvent.Push
    changes.send(Change(lastEvent, items.toList()))
  }

  override infix fun push(items: List<Screen>) {
    this.items += items
    lastEvent = StackEvent.Push
    changes.send(Change(lastEvent, items.toList()))
  }

  override fun replace(item: Screen) {
    if (items.isEmpty()) push(item)
    else items[items.lastIndex] = item
    lastEvent = StackEvent.Replace
    changes.send(Change(lastEvent, items.toList()))
  }

  override fun replaceAll(item: Screen) {
    items.clear()
    items += item
    lastEvent = StackEvent.Replace
    changes.send(Change(lastEvent, items.toList()))
  }
}
