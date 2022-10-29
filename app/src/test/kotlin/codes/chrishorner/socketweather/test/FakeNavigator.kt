package codes.chrishorner.socketweather.test

import app.cash.turbine.Turbine
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import codes.chrishorner.socketweather.util.Navigator
import kotlinx.coroutines.yield

class FakeNavigator(vararg initialScreens: Screen) : Navigator {

  data class Change(val event: StackEvent, val items: List<Screen>)

  private val changes = Turbine<Change>()

  suspend fun awaitChange(): Change {
    return changes.awaitItem()
  }

  suspend fun assertNoChanges() {
    yield() // Give other coroutines a chance to finish.
    changes.ensureAllEventsConsumed()
  }

  override val items: MutableList<Screen> = mutableListOf()

  override var lastEvent: StackEvent = StackEvent.Idle
    private set

  override val canPop: Boolean
    get() = items.size > 1

  override val isEmpty: Boolean
    get() = items.isEmpty()

  override val lastItemOrNull: Screen?
    get() = items.lastOrNull()

  @Deprecated("Use 'lastItemOrNull' instead. Will be removed in 1.0.0.", replaceWith = ReplaceWith("lastItemOrNull"))
  override val lastOrNull: Screen?
    get() = items.lastOrNull()

  override val size: Int
    get() = items.size

  init {
    items.addAll(initialScreens)
  }

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
      changes.add(Change(lastEvent, items.toList()))
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
    changes.add(Change(lastEvent, items.toList()))

    return success
  }

  override infix fun push(item: Screen) {
    items += item
    lastEvent = StackEvent.Push
    changes.add(Change(lastEvent, items.toList()))
  }

  override infix fun push(items: List<Screen>) {
    this.items += items
    lastEvent = StackEvent.Push
    changes.add(Change(lastEvent, items.toList()))
  }

  override fun replace(item: Screen) {
    if (items.isEmpty()) push(item)
    else items[items.lastIndex] = item
    lastEvent = StackEvent.Replace
    changes.add(Change(lastEvent, items.toList()))
  }

  override fun replaceAll(item: Screen) {
    items.clear()
    items += item
    lastEvent = StackEvent.Replace
    changes.add(Change(lastEvent, items.toList()))
  }
}
