@file:Suppress("unused") // It's useful to extend on MaterialTheme.colors.

package codes.chrishorner.socketweather.styles

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val md_theme_light_primary = Color(0xFF006494)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFCBE6FF)
val md_theme_light_onPrimaryContainer = Color(0xFF001E30)
val md_theme_light_secondary = Color(0xFF1C5FA5)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFD4E3FF)
val md_theme_light_onSecondaryContainer = Color(0xFF001C3A)
val md_theme_light_tertiary = Color(0xFF656100)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFEFE76C)
val md_theme_light_onTertiaryContainer = Color(0xFF1E1C00)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = Color(0xFFFAFCFF)
val md_theme_light_onBackground = Color(0xFF001F2A)
val md_theme_light_surface = Color(0xFFFAFCFF)
val md_theme_light_onSurface = Color(0xFF001F2A)
val md_theme_light_surfaceVariant = Color(0xFFDDE3EA)
val md_theme_light_onSurfaceVariant = Color(0xFF41474D)
val md_theme_light_outline = Color(0xFF72787E)
val md_theme_light_inverseOnSurface = Color(0xFFE1F4FF)
val md_theme_light_inverseSurface = Color(0xFF003547)
val md_theme_light_inversePrimary = Color(0xFF8ECDFF)
val md_theme_light_shadow = Color(0xFF000000)
val md_theme_light_surfaceTint = Color(0xFF006494)
val md_theme_light_outlineVariant = Color(0xFFC1C7CE)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = Color(0xFF8ECDFF)
val md_theme_dark_onPrimary = Color(0xFF00344F)
val md_theme_dark_primaryContainer = Color(0xFF004B71)
val md_theme_dark_onPrimaryContainer = Color(0xFFCBE6FF)
val md_theme_dark_secondary = Color(0xFFA5C8FF)
val md_theme_dark_onSecondary = Color(0xFF00315E)
val md_theme_dark_secondaryContainer = Color(0xFF004784)
val md_theme_dark_onSecondaryContainer = Color(0xFFD4E3FF)
val md_theme_dark_tertiary = Color(0xFFD2CA53)
val md_theme_dark_onTertiary = Color(0xFF343200)
val md_theme_dark_tertiaryContainer = Color(0xFF4C4800)
val md_theme_dark_onTertiaryContainer = Color(0xFFEFE76C)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = Color(0xFF001A23)
val md_theme_dark_onBackground = Color(0xFFBFE9FF)
val md_theme_dark_surface = Color(0xFF001A23)
val md_theme_dark_onSurface = Color(0xFFBFE9FF)
val md_theme_dark_surfaceVariant = Color(0xFF41474D)
val md_theme_dark_onSurfaceVariant = Color(0xFFC1C7CE)
val md_theme_dark_outline = Color(0xFF8B9198)
val md_theme_dark_inverseOnSurface = Color(0xFF001F2A)
val md_theme_dark_inverseSurface = Color(0xFFBFE9FF)
val md_theme_dark_inversePrimary = Color(0xFF006494)
val md_theme_dark_shadow = Color(0xFF000000)
val md_theme_dark_surfaceTint = Color(0xFF8ECDFF)
val md_theme_dark_outlineVariant = Color(0xFF41474D)
val md_theme_dark_scrim = Color(0xFF000000)

val seed = Color(0xFFA3D5FF)

val LightColorScheme = lightColorScheme(
  primary = md_theme_light_primary,
  onPrimary = md_theme_light_onPrimary,
  primaryContainer = md_theme_light_primaryContainer,
  onPrimaryContainer = md_theme_light_onPrimaryContainer,
  secondary = md_theme_light_secondary,
  onSecondary = md_theme_light_onSecondary,
  secondaryContainer = md_theme_light_secondaryContainer,
  onSecondaryContainer = md_theme_light_onSecondaryContainer,
  tertiary = md_theme_light_tertiary,
  onTertiary = md_theme_light_onTertiary,
  tertiaryContainer = md_theme_light_tertiaryContainer,
  onTertiaryContainer = md_theme_light_onTertiaryContainer,
  error = md_theme_light_error,
  errorContainer = md_theme_light_errorContainer,
  onError = md_theme_light_onError,
  onErrorContainer = md_theme_light_onErrorContainer,
  background = md_theme_light_background,
  onBackground = md_theme_light_onBackground,
  surface = md_theme_light_surface,
  onSurface = md_theme_light_onSurface,
  surfaceVariant = md_theme_light_surfaceVariant,
  onSurfaceVariant = md_theme_light_onSurfaceVariant,
  outline = md_theme_light_outline,
  inverseOnSurface = md_theme_light_inverseOnSurface,
  inverseSurface = md_theme_light_inverseSurface,
  inversePrimary = md_theme_light_inversePrimary,
  surfaceTint = md_theme_light_surfaceTint,
  outlineVariant = md_theme_light_outlineVariant,
  scrim = md_theme_light_scrim,
)

val DarkColorScheme = darkColorScheme(
  primary = md_theme_dark_primary,
  onPrimary = md_theme_dark_onPrimary,
  primaryContainer = md_theme_dark_primaryContainer,
  onPrimaryContainer = md_theme_dark_onPrimaryContainer,
  secondary = md_theme_dark_secondary,
  onSecondary = md_theme_dark_onSecondary,
  secondaryContainer = md_theme_dark_secondaryContainer,
  onSecondaryContainer = md_theme_dark_onSecondaryContainer,
  tertiary = md_theme_dark_tertiary,
  onTertiary = md_theme_dark_onTertiary,
  tertiaryContainer = md_theme_dark_tertiaryContainer,
  onTertiaryContainer = md_theme_dark_onTertiaryContainer,
  error = md_theme_dark_error,
  errorContainer = md_theme_dark_errorContainer,
  onError = md_theme_dark_onError,
  onErrorContainer = md_theme_dark_onErrorContainer,
  background = md_theme_dark_background,
  onBackground = md_theme_dark_onBackground,
  surface = md_theme_dark_surface,
  onSurface = md_theme_dark_onSurface,
  surfaceVariant = md_theme_dark_surfaceVariant,
  onSurfaceVariant = md_theme_dark_onSurfaceVariant,
  outline = md_theme_dark_outline,
  inverseOnSurface = md_theme_dark_inverseOnSurface,
  inverseSurface = md_theme_dark_inverseSurface,
  inversePrimary = md_theme_dark_inversePrimary,
  surfaceTint = md_theme_dark_surfaceTint,
  outlineVariant = md_theme_dark_outlineVariant,
  scrim = md_theme_dark_scrim,
)
