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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.styles.ScrimColor
import com.google.accompanist.insets.systemBarsPadding

@Composable
fun LocationSwitcher(
  visible: Boolean,
  currentLocation: LocationEntry,
  savedLocations: List<LocationEntry>,
  onDismissRequest: () -> Unit,
  onEvent: (HomeEvent) -> Unit,
) {

  val visibleStates = remember { MutableTransitionState(false) }
  visibleStates.targetState = visible

  val currentlyVisibility = visibleStates.currentState || visibleStates.targetState

  BackHandler(enabled = currentlyVisibility) {
    onDismissRequest()
  }

  val transition = updateTransition(targetState = visible, label = "LocationChooser")

  val scale by transition.animateFloat(
    transitionSpec = {
      if (false isTransitioningTo true) {
        tween(durationMillis = EnterDurationMs, easing = LinearOutSlowInEasing)
      } else {
        tween(durationMillis = ExitDurationMs, easing = FastOutLinearInEasing)
      }
    },
    label = "LocationChooser.scale"
  ) {
    if (it) 1f else 0.8f
  }

  val alpha by transition.animateFloat(
    transitionSpec = {
      if (false isTransitioningTo true) {
        tween(durationMillis = EnterDurationMs)
      } else {
        tween(durationMillis = ExitDurationMs)
      }
    },
    label = "LocationChooser.scale"
  ) {
    if (it) 1f else 0f
  }

  if (currentlyVisibility) {
    Box {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable(
            // Disable touch ripple.
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) { onDismissRequest() }
          .background(ScrimColor)
          .alpha(alpha)
      )

      Card(
        modifier = Modifier
          .systemBarsPadding()
          .padding(horizontal = 8.dp)
          .fillMaxWidth()
          .wrapContentHeight()
          .graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
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
          painterResource(R.drawable.ic_check_circle_outline_24dp),
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
        painterResource(R.drawable.ic_arrow_drop_up_24dp),
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
                painterResource(R.drawable.ic_my_location_24dp),
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
          painterResource(R.drawable.ic_add_24dp),
          contentDescription = null,
          modifier = Modifier.padding(horizontal = 20.dp)
        )
      }
      Text(stringResource(R.string.switchLocation_add), style = MaterialTheme.typography.button)
    }
  }
}

private const val EnterDurationMs = 120
private const val ExitDurationMs = 75