@file:Suppress("unused") // Even if things aren't used, we want to adhere to the Material spec.

package codes.chrishorner.socketweather.styles

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import codes.chrishorner.socketweather.R

private val Inter = FontFamily(
  Font(R.font.inter_regular),
  Font(R.font.inter_medium, weight = FontWeight.Medium)
)

private val ZillaSlab = FontFamily(
  Font(R.font.zilla_slab_medium, weight = FontWeight.Medium)
)

private val RobotoSlab = FontFamily(
  Font(R.font.roboto_slab_regular),
  Font(R.font.roboto_slab_medium, weight = FontWeight.Medium)
)

val SocketWeatherTypography = Typography(
  displayLarge = TextStyle(
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium,
    fontSize = 96.sp,
    lineHeight = TextUnit.Unspecified,
    letterSpacing = (-1.5).sp,
  ),
  displayMedium = TextStyle(
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium,
    fontSize = 60.sp,
    lineHeight = 66.sp,
    letterSpacing = (-0.5).sp,
  ),
  displaySmall = TextStyle(
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium,
    fontSize = 48.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp,
  ),
  headlineLarge = TextStyle(
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium,
    fontSize = 40.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium,
    fontSize = 34.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.25.sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.2.sp,
  ),
  titleLarge = TextStyle(
    fontFamily = ZillaSlab,
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.15.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.2.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = TextUnit.Unspecified,
  ),
  bodyMedium = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.02.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
  ),
)

private val LargeTempTextStyle = SocketWeatherTypography.displayMedium.copy(
  fontSize = 64.sp,
  fontFamily = RobotoSlab,
  fontWeight = FontWeight.Normal
)

private val MediumTempTextStyle = SocketWeatherTypography.headlineSmall.copy(
  fontFamily = RobotoSlab,
  fontWeight = FontWeight.Medium
)

private val SmallTempTextStyle = SocketWeatherTypography.bodyLarge.copy(
  fontSize = 18.sp,
  fontFamily = RobotoSlab,
  fontWeight = FontWeight.Medium
)

private val TinyTempTextStyle = SocketWeatherTypography.bodyLarge.copy(
  fontSize = 16.sp,
  fontFamily = RobotoSlab,
  fontWeight = FontWeight.Medium
)

private val CopyrightTextStyle = SocketWeatherTypography.bodySmall.copy(fontSize = 10.sp)

val Typography.largeTemp
  get() = LargeTempTextStyle

val Typography.mediumTemp
  get() = MediumTempTextStyle

val Typography.smallTemp
  get() = SmallTempTextStyle

val Typography.copyright
  get() = CopyrightTextStyle
