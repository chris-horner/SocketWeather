package codes.chrishorner.socketweather.styles

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun SocketWeatherTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit
) {
  val context = LocalContext.current
  MaterialTheme(
    colorScheme = when {
      dynamicColor && darkTheme -> dynamicDarkColorScheme(context)
      dynamicColor && !darkTheme -> dynamicLightColorScheme(context)
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    },
    typography = SocketWeatherTypography,
    content = content
  )
}
