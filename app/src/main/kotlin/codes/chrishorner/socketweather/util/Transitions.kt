package codes.chrishorner.socketweather.util

import android.transition.Transition
import android.transition.TransitionSet

/**
 * Allows for building transitions in a DSL.
 */
inline fun transitionSet(block: TransitionSet.() -> Unit): TransitionSet {
  return TransitionSet().apply(block)
}

inline fun TransitionSet.addTransition(transition: Transition, block: Transition.() -> Unit) {
  transition.apply(block)
  addTransition(transition)
}
