package codes.chrishorner.socketweather.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import codes.chrishorner.socketweather.R.drawable
import codes.chrishorner.socketweather.styles.SmallTempTextStyle
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * The minimum number of degrees to show on the graph. This means that if the temperature
 * isn't changing much over time, (unheard of in Melbourne), then the line graph won't
 * fluctuate too aggressively for small changes.
 */
private const val MIN_RANGE_OF_DEGREES = 8

data class TimeForecastGraphItem(
  val temperatureC: Int,
  val formattedTemperature: String,
  val time: String,
  val rainChancePercent: Int,
  val formattedRainChance: String
)

private data class TemperatureScale(val min: Int, val max: Int) {
  val range = abs(min - max)
}

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
  val totalGraphHeight = 208.dp
  val columnWidth = 64.dp
  val verticalPadding = 16.dp
  val rainChanceHeight: Dp
  val temperatureTextHeight: Dp
  val lineGraphPadding = 16.dp
  val lineGraphHeight: Dp
  val timeTextHeight: Dp
  val linePointGap = 12.dp

  val showChanceOfRain = entries.any { it.rainChancePercent > 0 }
  val rainIconPainter = painterResource(drawable.ic_water)
  val scale = getTemperatureScale(entries)
  val listState = rememberLazyListState()
  val foregroundColour = MaterialTheme.colors.onBackground
  val secondaryColour = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)

  with(LocalDensity.current) {
    val rainTextHeight = MaterialTheme.typography.overline.fontSize.toDp()
    val rainIconHeight = rainIconPainter.intrinsicSize.height.toDp()
    temperatureTextHeight = SmallTempTextStyle.fontSize.toDp()
    rainChanceHeight = if (showChanceOfRain) max(rainTextHeight, rainIconHeight) else 0.dp
    timeTextHeight = MaterialTheme.typography.overline.fontSize.toDp()
    lineGraphHeight = totalGraphHeight - rainChanceHeight - temperatureTextHeight - timeTextHeight
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = verticalPadding)
      .height(totalGraphHeight)
  ) {

    LazyRow(state = listState, modifier = Modifier.fillMaxHeight()) {
      items(entries) { entry ->
        Column(
          modifier = Modifier
            .width(columnWidth)
            .fillMaxHeight(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          if (showChanceOfRain) {
            if (entry.rainChancePercent > 0) {
              Row(modifier = Modifier.requiredHeight(rainChanceHeight)) {
                CompositionLocalProvider(LocalContentAlpha provides 0.2f) {
                  Icon(painter = rainIconPainter, contentDescription = "") // Not important for accessibility.
                }
                Text(entry.formattedRainChance, style = MaterialTheme.typography.overline)
              }
            } else {
              Spacer(modifier = Modifier.requiredHeight(rainChanceHeight))
            }
          }

          BoxWithConstraints(
            modifier = Modifier
              .height(lineGraphHeight)
              .padding(vertical = lineGraphPadding),
            contentAlignment = Alignment.BottomCenter
          ) {
            val normalisedY = (entry.temperatureC - scale.min) / scale.range.toFloat()
            val yOffset = (-maxHeight + temperatureTextHeight) * normalisedY
            Text(
              text = entry.formattedTemperature,
              style = SmallTempTextStyle,
              modifier = Modifier.offset(y = yOffset - 8.dp)
            )
          }

          Text(entry.time, style = MaterialTheme.typography.overline)
        }
      }
    }

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(lineGraphHeight)
        .padding(vertical = lineGraphPadding)
        .offset(y = rainChanceHeight)
    ) {
      var previousX = -1f
      var previousY = -1f

      val firstEntryIndex = listState.firstVisibleItemIndex
      val lastEntryIndex =
        (firstEntryIndex + (size.width / columnWidth.toPx()).toInt() + 1).coerceAtMost(entries.size - 1)

      var columnIndex = 0

      @Suppress("UseWithIndex") // Avoid allocating an iterator.
      for (entryIndex in firstEntryIndex..lastEntryIndex) {
        val entry = entries[entryIndex]
        val normalisedY = (entry.temperatureC - scale.min) / scale.range.toFloat()
        val availableHeight = size.height - temperatureTextHeight.toPx()
        val x = (columnWidth.toPx() / 2) + (columnWidth.toPx() * columnIndex) - listState.firstVisibleItemScrollOffset
        val y = temperatureTextHeight.toPx() + availableHeight - (availableHeight * normalisedY)

        drawCircle(
          color = foregroundColour,
          radius = 3.dp.toPx(),
          center = Offset(x, y)
        )

        if (previousX != -1f && previousY != -1f) {
          val slope = (y - previousY) / (x - previousX)
          val start: Offset
          val end: Offset

          if (slope == 0f) {
            start = Offset(x = previousX + linePointGap.toPx(), y = previousY)
            end = Offset(x = x - linePointGap.toPx(), y = previousY)
          } else {
            val dx = linePointGap.toPx() / sqrt(1 + (slope * slope))
            val dy = slope * dx
            start = Offset(x = previousX + dx, y = previousY + dy)
            end = Offset(x = x - dx, y = y - dy)
          }

          drawLine(secondaryColour, start, end, strokeWidth = 3.dp.toPx(), StrokeCap.Round)
        }

        previousX = x
        previousY = y
        columnIndex++
      }
    }
  }
}

/*
private fun TimeForecastGraphItem.getGraphY(graphHeight: Dp, scale: TemperatureScale): Dp {
  val normalisedY = (temperatureC - scale.min) / scale.range.toFloat()
  val y = temperatureTextHeight + graphHeight - (graphHeight * normalisedY)
}
 */

private fun getTemperatureScale(items: List<TimeForecastGraphItem>): TemperatureScale {
  val temperatures = items.map { it.temperatureC }
  var min = temperatures.minOrNull() ?: 0
  var max = temperatures.maxOrNull() ?: 0
  val scale = abs(max - min)

  // Check the range at which the temperature is changing. If it's lower than our minimum,
  // then force the min and max temperatures to adhere to MIN_RANGE_OF_DEGREES.
  if (scale < MIN_RANGE_OF_DEGREES) {
    val tempDelta = (MIN_RANGE_OF_DEGREES - scale) / 2
    min -= tempDelta
    max += tempDelta
  }

  return TemperatureScale(min, max)
}
