package codes.chrishorner.socketweather.util.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import cafe.adriel.voyager.transitions.ScreenTransitionContent

private const val FadeOutDuration = 90
private const val FadeInDuration = 210
private const val ScaleDuration = 300
private const val IncomingInitialScale = 0.8f
private const val OutgoingTargetScale = 1.1f

@Composable
fun FadeThroughTransition(
  navigator: Navigator,
  content: ScreenTransitionContent = { it.Content() }
) {
  val popping = navigator.lastEvent == StackEvent.Pop
  val outgoingTargetScale = if (popping) IncomingInitialScale else OutgoingTargetScale
  val incomingInitialScale = if (popping) OutgoingTargetScale else IncomingInitialScale

  ScreenTransition(
    navigator = navigator,
    content = content,
    transition = {
      ContentTransform(
        initialContentExit = fadeOut(
          animationSpec = tween(FadeOutDuration, easing = FastOutLinearInEasing)
        ) + scaleOut(
          animationSpec = tween(ScaleDuration, easing = FastOutLinearInEasing),
          targetScale = outgoingTargetScale
        ),
        targetContentEnter = fadeIn(
          animationSpec = tween(FadeInDuration, delayMillis = FadeOutDuration, easing = LinearOutSlowInEasing)
        ) + scaleIn(
          animationSpec = tween(ScaleDuration, easing = LinearOutSlowInEasing),
          initialScale = incomingInitialScale,
        )
      )
    }
  )
}