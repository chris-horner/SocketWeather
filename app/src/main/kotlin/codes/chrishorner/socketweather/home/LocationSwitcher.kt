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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
) {

  val visibleStates = remember { MutableTransitionState(false) }
  visibleStates.targetState = visible

  val currentlyVisibility = visibleStates.currentState || visibleStates.targetState

  BackHandler(enabled = currentlyVisibility) {
    onDismissRequest()
  }

  val transition = updateTransition(targetState = visible, label = "LocationChooser")

  val scale by transition.animateFloat(transitionSpec = {
    if (false isTransitioningTo true) {
      tween(durationMillis = EnterDurationMs, easing = LinearOutSlowInEasing)
    } else {
      tween(durationMillis = ExitDurationMs, easing = FastOutLinearInEasing)
    }
  }, label = "LocationChooser.scale") {
    if (it) 1f else 0.8f
  }

  val alpha by transition.animateFloat(
    transitionSpec = {
      if (false isTransitioningTo true) {
        tween(durationMillis = EnterDurationMs)
      } else {
        tween(durationMillis = ExitDurationMs)
      }
    }, label = "LocationChooser.scale"
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
        LocationSwitcherContent(currentLocation, savedLocations)
      }
    }
  }
}

@Composable
private fun LocationSwitcherContent(currentLocation: LocationEntry, savedLocations: List<LocationEntry>) {
  Column {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(painterResource(R.drawable.ic_check_circle_outline_24dp), contentDescription = null)
      Column {
        Text(currentLocation.title)
        Text(currentLocation.subtitle)
      }
      Icon(painterResource(R.drawable.ic_arrow_drop_up_24dp), contentDescription = null)
    }
    LazyColumn {
      item {

      }

      items(6) { Text("Hello") }
    }
    Row {
      Icon(painterResource(R.drawable.ic_add_24dp), contentDescription = null)
      Text(stringResource(R.string.switchLocation_add))
    }
  }
}

private const val EnterDurationMs = 120
private const val ExitDurationMs = 75