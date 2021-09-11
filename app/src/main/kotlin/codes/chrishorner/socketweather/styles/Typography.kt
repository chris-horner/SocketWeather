package codes.chrishorner.socketweather.styles

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import codes.chrishorner.socketweather.R.font

private val Rubik = FontFamily(
  Font(font.rubik_regular),
  Font(font.rubik_medium, weight = FontWeight.Medium)
)

private val ZillaSlab = FontFamily(
  Font(font.zilla_slab_medium, weight = FontWeight.Medium)
)

private val RobotoSlab = FontFamily(
  Font(font.roboto_slab_regular),
  Font(font.roboto_slab_medium, weight = FontWeight.Medium)
)

val SocketWeatherTypography = Typography(
  defaultFontFamily = Rubik,
  h1 = TextStyle(
    fontSize = 96.sp,
    letterSpacing = (-1.5).sp,
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium
  ),
  h2 = TextStyle(
    fontSize = 60.sp,
    letterSpacing = (-0.5).sp,
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium
  ),
  h3 = TextStyle(
    fontSize = 48.sp,
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium
  ),
  h4 = TextStyle(
    fontSize = 34.sp,
    letterSpacing = 0.25.sp,
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium
  ),
  h5 = TextStyle(
    fontSize = 24.sp,
    letterSpacing = 0.2.sp,
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium
  ),
  h6 = TextStyle(
    fontSize = 20.sp,
    letterSpacing = 0.15.sp,
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium
  ),
  subtitle1 = TextStyle(
    fontSize = 18.sp,
    letterSpacing = 0.2.sp,
    fontWeight = FontWeight.Normal
  ),
  subtitle2 = TextStyle(
    fontSize = 14.sp,
    letterSpacing = 0.1.sp,
    fontWeight = FontWeight.Normal
  ),
  overline = TextStyle(
    fontSize = 15.sp
  ),
  body1 = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    letterSpacing = 0.8.sp,
    lineHeight = 24.sp,
  )
)

val LargeTempTextStyle = SocketWeatherTypography.h2.copy(
  fontSize = 64.sp,
  fontFamily = RobotoSlab,
  fontWeight = FontWeight.Normal
)

val MediumTempTextStyle = SocketWeatherTypography.h5.copy(
  fontFamily = RobotoSlab,
  fontWeight = FontWeight.Medium
)

val SmallTempTextStyle = SocketWeatherTypography.body1.copy(
  fontSize = 18.sp,
  fontFamily = RobotoSlab,
  fontWeight = FontWeight.Medium
)

val TinyTempTextStyle = SocketWeatherTypography.body1.copy(
  fontSize = 16.sp,
  fontFamily = RobotoSlab,
  fontWeight = FontWeight.Medium
)

val CopyrightTextStyle = SocketWeatherTypography.caption.copy(fontSize = 10.sp)
