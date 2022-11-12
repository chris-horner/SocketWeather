package codes.chrishorner.socketweather

import android.content.Context
import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

/**
 * Abstracts whatever flavour of navigation library we happen to be using this week.
 *
 * Given to [Presenter] instances to navigate to [Screen] destinations.
 */
interface Navigator {
  val canPop: Boolean
  fun push(screen: Screen<*, *>)
  fun pop()
  fun replaceAllWith(screen: Screen<*, *>)
}

/**
 * Represents a navigation destination.
 *
 * Is responsible for providing:
 * - A [Presenter] that produces state for the screen and handles events.
 * - A UI composable
 */
interface Screen<Event, State> : Parcelable {
  fun onCreatePresenter(context: Context, navigator: Navigator): Presenter<Event, State>

  @Composable
  fun Content(state: State, onEvent: (Event) -> Unit)
}

/**
 * Takes a stream of events and produces state over time for a [Screen].
 */
interface Presenter<Event, State> {
  @Composable
  fun states(events: Flow<Event>): State
}
