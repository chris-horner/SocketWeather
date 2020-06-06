package codes.chrishorner.socketweather.util

import androidx.transition.Transition
import androidx.transition.TransitionSet

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
