package codes.chrishorner.socketweather.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.styles.SmallTempTextStyle
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import kotlin.math.abs

/**
 * The minimum number of degrees to show on the graph. This means that if the temperature
 * isn't changing much over time, (unheard of in Melbourne), then the line graph won't
 * fluctuate too aggressively for small changes.
 */
private const val MIN_SCALE_OF_DEGREES = 8

data class TimeForecastGraphItem(
  val temperatureC: Int,
  val formattedTemperature: String,
  val time: String,
  val rainChancePercent: Int,
  val formattedRainChance: String
)

@Preview(showBackground = true)
@Composable
private fun GraphPreview() {
  SocketWeatherTheme {
    TimeForecastGraph(
      entries = listOf(
        TimeForecastGraphItem(20, "20°", "8 AM", 0, ""),
        TimeForecastGraphItem(22, "22°", "11 AM", 10, "10%"),
        TimeForecastGraphItem(18, "18°", "1 PM", 20, "20%"),
        TimeForecastGraphItem(16, "16°", "4 PM", 80, "80%"),
        TimeForecastGraphItem(12, "12°", "7 PM", 70, "70%"),
        TimeForecastGraphItem(9, "9°", "10 PM", 20, "20%"),
        TimeForecastGraphItem(8, "8°", "1 AM", 0, ""),
      )
    )
  }
}

@Composable
fun TimeForecastGraph(entries: List<TimeForecastGraphItem>) {
  val showChanceOfRain = entries.any { it.rainChancePercent > 0 }
  val rainIconPainter = painterResource(R.drawable.ic_water)
  val temperatures = entries.map { it.temperatureC }
  var minTemp = temperatures.minOrNull() ?: 0
  var maxTemp = temperatures.maxOrNull() ?: 0
  var scale = abs(maxTemp - minTemp)
  var graphHeight = 0.dp
  val listState = rememberLazyListState()
  val temperatureTextHeight: Dp
  val rainChanceHeight: Dp

  with(LocalDensity.current) {
    val rainTextHeight = MaterialTheme.typography.overline.fontSize.toDp()
    val rainIconHeight = rainIconPainter.intrinsicSize.height.toDp()
    rainChanceHeight = max(rainTextHeight, rainIconHeight)
    temperatureTextHeight = SmallTempTextStyle.fontSize.toDp()
  }

  // Check the scale at which the temperature is changing. If it's lower than our minimum,
  // then force the min and max temperatures to adhere to MIN_SCALE_OF_DEGREES.
  if (scale < MIN_SCALE_OF_DEGREES) {
    val tempDelta = (MIN_SCALE_OF_DEGREES - scale) / 2
    minTemp -= tempDelta
    maxTemp += tempDelta
    scale = MIN_SCALE_OF_DEGREES
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 8.dp, bottom = 8.dp)
      .height(208.dp)
  ) {

    LazyRow(state = listState, modifier = Modifier.requiredHeight(208.dp)) {
      items(entries) { entry ->
        Column(
          modifier = Modifier
            .requiredWidth(64.dp)
            .requiredHeight(208.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          if (showChanceOfRain) {
            if (entry.rainChancePercent > 0) {
              Row(modifier = Modifier.requiredHeight(rainChanceHeight)) {
                Icon(painter = rainIconPainter, contentDescription = "") // Not important for accessibility.
                Text(entry.formattedRainChance, style = MaterialTheme.typography.overline)
              }
            } else {
              Spacer(modifier = Modifier.requiredHeight(rainChanceHeight))
            }
          }

          BoxWithConstraints(
            modifier = Modifier
              .weight(1f)
              .padding(top = 16.dp, bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter
          ) {
            graphHeight = maxHeight

            val normalisedY = (entry.temperatureC - minTemp) / scale.toFloat()
            val yOffset = (-maxHeight + temperatureTextHeight) * normalisedY

            Text(
              text = entry.formattedTemperature,
              style = SmallTempTextStyle,
              modifier = Modifier.offset(y = yOffset - 8.dp)
            )
            Box(
              modifier = Modifier
                .offset(y = yOffset)
                .requiredSize(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colors.onBackground)
            )
          }

          Text(entry.time, style = MaterialTheme.typography.overline)
        }
      }
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()
    ) {
    }
  }
}
