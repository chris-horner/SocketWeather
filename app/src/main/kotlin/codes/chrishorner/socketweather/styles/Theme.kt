package codes.chrishorner.socketweather.styles

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun SocketWeatherTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
  MaterialTheme(
      colors = if (darkTheme) DarkColors else LightColors,
      typography = SocketWeatherTypography,
      content = content
  )
}
