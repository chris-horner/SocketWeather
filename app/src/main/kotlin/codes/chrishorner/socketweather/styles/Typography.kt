package codes.chrishorner.socketweather.styles

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.font
import androidx.compose.ui.text.font.fontFamily
import androidx.compose.ui.unit.sp
import codes.chrishorner.socketweather.R.font

private val Rubik = fontFamily(
    font(font.rubik_regular),
    font(font.rubik_medium, weight = FontWeight.Medium)
)

private val ZillaSlab = fontFamily(
    font(font.zilla_slab_medium, weight = FontWeight.Medium)
)

private val RobotoSlab = fontFamily(
    font(font.roboto_slab_regular),
    font(font.roboto_slab_medium, weight = FontWeight.Medium)
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
