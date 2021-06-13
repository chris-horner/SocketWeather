package codes.chrishorner.socketweather.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.styles.scrim
import com.google.accompanist.insets.systemBarsPadding

@Composable
fun LocationSwitcher(
  visible: Boolean,
  currentLocation: LocationEntry,
  savedLocations: List<LocationEntry>,
  onDismissRequest: () -> Unit,
  onEvent: (HomeEvent) -> Unit,
) {
  val transition = updateVisibilityTransition(visible)

  BackHandler(enabled = transition.currentlyVisible) {
    onDismissRequest()
  }

  if (transition.currentlyVisible) {
    Box {
      // Render scrim behind card.
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable(
            // Disable touch ripple.
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) { onDismissRequest() }
          .alpha(transition.scrimAlpha)
          .background(MaterialTheme.colors.scrim)
      )

      Card(
        elevation = 8.dp,
        modifier = Modifier
          .systemBarsPadding()
          .padding(horizontal = 8.dp)
          .fillMaxWidth()
          .wrapContentHeight()
          .graphicsLayer {
            scaleX = transition.cardScale
            scaleY = transition.cardScale
            alpha = transition.cardAlpha
            transformOrigin = TransformOrigin(0.5f, 0.1f)
          }
      ) {
        LocationSwitcherContent(currentLocation, savedLocations, onDismissRequest, onEvent)
      }
    }
  }
}

@Composable
private fun LocationSwitcherContent(
  currentLocation: LocationEntry,
  savedLocations: List<LocationEntry>,
  onDismissRequest: () -> Unit,
  onEvent: (HomeEvent) -> Unit
) {
  Column {

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .height(64.dp)
        .clickable { onDismissRequest() },
    ) {
      CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Icon(
          Icons.Rounded.CheckCircleOutline,
          contentDescription = null,
          modifier = Modifier.padding(horizontal = 20.dp)
        )
      }
      Column {
        Text(currentLocation.title, style = MaterialTheme.typography.h6)
        Text(currentLocation.subtitle, style = MaterialTheme.typography.subtitle2)
      }
      Spacer(modifier = Modifier.weight(1f))
      Icon(
        Icons.Rounded.ArrowDropUp,
        contentDescription = null,
        Modifier.padding(horizontal = 16.dp)
      )
    }

    Divider()

    LazyColumn {
      items(savedLocations) { item ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clickable { onEvent(HomeEvent.SwitchLocation(item.selection)) },
        ) {
          if (item.showTrackingIcon) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
              Icon(
                Icons.Rounded.MyLocation,
                contentDescription = null,
                Modifier.padding(horizontal = 20.dp)
              )
            }
          } else {
            Spacer(modifier = Modifier.width(64.dp))
          }
          Column {
            Text(item.title, style = MaterialTheme.typography.h6)
            Text(item.subtitle, style = MaterialTheme.typography.subtitle2)
          }
        }
      }
    }

    Divider()

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .clickable { onEvent(HomeEvent.AddLocation) },
    ) {
      CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Icon(
          Icons.Rounded.Add,
          contentDescription = null,
          modifier = Modifier.padding(horizontal = 20.dp)
        )
      }
      Text(stringResource(R.string.switchLocation_add), style = MaterialTheme.typography.button)
    }
  }
}

private const val CardEnterDurationMs = 120
private const val CardExitDurationMs = 75
private const val ScrimEnterDurationMs = 250
private const val ScrimExitDurationMs = 75

private class TransitionData(
  currentlyVisible: State<Boolean>,
  cardScale: State<Float>,
  cardAlpha: State<Float>,
  scrimAlpha: State<Float>,
) {
  val currentlyVisible by currentlyVisible
  val cardScale by cardScale
  val cardAlpha by cardAlpha
  val scrimAlpha by scrimAlpha
}

@Composable
private fun updateVisibilityTransition(visible: Boolean): TransitionData {

  val visibilityState = remember { MutableTransitionState(initialState = false) }
  visibilityState.targetState = visible
  val currentlyVisible = derivedStateOf { visibilityState.targetState || visibilityState.currentState }
  val transition = updateTransition(visibilityState, label = "LocationChooser")

  val cardScale = transition.animateFloat(
    label = "LocationChooser.cardScale",
    transitionSpec = {
      if (false isTransitioningTo true) {
        tween(durationMillis = CardEnterDurationMs, easing = LinearOutSlowInEasing)
      } else {
        tween(durationMillis = CardExitDurationMs, easing = FastOutLinearInEasing)
      }
    }
  ) { cardVisible ->
    if (cardVisible) 1f else 0.8f
  }

  val cardAlpha = transition.animateFloat(
    label = "LocationChooser.cardAlpha",
    transitionSpec = {
      if (false isTransitioningTo true) {
        tween(durationMillis = CardEnterDurationMs)
      } else {
        tween(durationMillis = CardExitDurationMs)
      }
    }
  ) { cardVisible ->
    if (cardVisible) 1f else 0f
  }

  val scrimAlpha = transition.animateFloat(
    label = "LocationChooser.scrimAlpha",
    transitionSpec = {
      if (false isTransitioningTo true) {
        tween(durationMillis = ScrimEnterDurationMs)
      } else {
        tween(durationMillis = ScrimExitDurationMs)
      }
    }
  ) { scrimVisible ->
    if (scrimVisible) 1f else 0f
  }

  return remember(transition) { TransitionData(currentlyVisible, cardScale, cardAlpha, scrimAlpha) }
}
