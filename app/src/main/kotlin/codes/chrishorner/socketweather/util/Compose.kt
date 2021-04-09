package codes.chrishorner.socketweather.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.statusBarsPadding

/**
 * A wrapper around [TopAppBar] which uses [Modifier.statusBarsPadding] to shift the app bar's
 * contents down, but still draws the background behind the status bar too.
 *
 * Thanks Chris: https://github.com/chrisbanes/accompanist/blob/main/sample/src/main/java/dev/chrisbanes/accompanist/sample/insets/EdgeToEdgeLazyColumn.kt
 */
@Composable
fun InsetAwareTopAppBar(
  title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  navigationIcon: @Composable (() -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {},
  backgroundColor: Color = MaterialTheme.colors.primarySurface,
  contentColor: Color = contentColorFor(backgroundColor),
  elevation: Dp = 4.dp
) {
  Surface(
    color = backgroundColor,
    elevation = elevation,
    modifier = modifier
  ) {
    TopAppBar(
      title = title,
      navigationIcon = navigationIcon,
      actions = actions,
      backgroundColor = Color.Transparent,
      contentColor = contentColor,
      elevation = 0.dp,
      modifier = Modifier.statusBarsPadding()
    )
  }
}

@Composable
fun ThickDivider(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .height(4.dp)
      .clip(RoundedCornerShape(2.dp))
      .background(MaterialTheme.colors.onBackground.copy(alpha = 0.2f))
  )
}
