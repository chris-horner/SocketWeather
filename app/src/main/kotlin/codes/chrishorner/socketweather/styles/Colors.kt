package codes.chrishorner.socketweather.styles

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightestBlue = Color(0xfff5fcfe)
private val LightBlue = Color(0xffc1e4ee)
private val Blue = Color(0xff00abe3)
private val Orange = Color(0xfffb9039)
private val LightOrange = Color(0xfff2a363)
private val DarkGrey = Color(0xff121212)
private val ScrimLight = Color(0x44000000)
private val ScrimDark = Color(0xAA000000)

val LightColors = lightColors(
    primary = LightBlue,
    primaryVariant = Blue,
    onPrimary = Color.Black,
    secondary = Orange,
    secondaryVariant = LightOrange,
    onSecondary = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    background = LightestBlue,
    onBackground = Color.Black
)

val DarkColors = darkColors(
    primary = LightBlue,
    primaryVariant = Blue,
    onPrimary = Color.Black,
    secondary = LightOrange,
    onSecondary = Color.Black,
    surface = DarkGrey,
    onSurface = Color.White,
)

val ScrimColor: Color
  @Composable
  get() = if (MaterialTheme.colors.isLight) ScrimLight else ScrimDark
