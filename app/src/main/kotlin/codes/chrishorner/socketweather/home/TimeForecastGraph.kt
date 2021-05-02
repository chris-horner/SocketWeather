package codes.chrishorner.socketweather.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.R
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
  val topSectionHeight: Dp // Top area that contains chance of rain (if applicable).
  val temperatureTextHeight: Dp
  val listGraphHeight: Dp // Total height in which an entry's temperature will appear.
  val lineGraphHeight: Dp // Total height available for the line graph to render.
  val bottomSectionHeight = 40.dp // Bottom area that displays the entry's time.
  val columnWidth = 64.dp
  val linePointGap = 12.dp // Visual space between dots and lines on the graph.

  val showChanceOfRain = entries.any { it.rainChancePercent > 0 }
  val rainIconPainter = painterResource(R.drawable.ic_water)
  val scale = getTemperatureScale(entries)
  val listState = rememberLazyListState()
  val foregroundColour = MaterialTheme.colors.onBackground
  val fadedColour = MaterialTheme.colors.onBackground.copy(alpha = 0.2f)

  with(LocalDensity.current) {
    topSectionHeight = if (showChanceOfRain) 40.dp else 0.dp
    listGraphHeight = totalGraphHeight - topSectionHeight - bottomSectionHeight
    temperatureTextHeight = SmallTempTextStyle.fontSize.toDp() + 8.dp // Add 8dp to approximate the actual height.
    lineGraphHeight = listGraphHeight - temperatureTextHeight
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(totalGraphHeight)
  ) {

    // Start by drawing chance of rain, temperature, and time as LazyRow items.

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
              Row(
                modifier = Modifier
                  .height(topSectionHeight)
                  .padding(bottom = 16.dp),
                verticalAlignment = Alignment.Bottom
              ) {
                Icon(
                  painter = rainIconPainter,
                  contentDescription = "", // Not important for accessibility.
                  tint = fadedColour
                )
                Text(entry.formattedRainChance, style = MaterialTheme.typography.overline)
              }
            } else {
              Spacer(modifier = Modifier.height(topSectionHeight))
            }
          }

          Box(modifier = Modifier.height(listGraphHeight)) {
            val yOffset = entry.getGraphY(lineGraphHeight, scale) - 6.dp
            Text(
              text = entry.formattedTemperature,
              style = SmallTempTextStyle,
              modifier = Modifier.offset(y = yOffset),
            )
          }

          Text(
            text = entry.time,
            style = MaterialTheme.typography.overline,
            modifier = Modifier
              .height(bottomSectionHeight)
              .padding(top = 12.dp)
          )
        }
      }
    }

    // Next, inspect the LazyRow's list state and determine where and what to draw
    // for the line graph.

    Canvas(
      modifier = Modifier
        .fillMaxWidth()
        .height(lineGraphHeight)
        .offset(y = topSectionHeight + temperatureTextHeight)
    ) {

      // Determine which entries are currently on screen.

      val firstEntryIndex = listState.firstVisibleItemIndex
      val lastEntryIndex =
        (firstEntryIndex + (size.width / columnWidth.toPx()).toInt() + 1).coerceAtMost(entries.size - 1)
      var columnIndex = 0

      // Iterate through every currently visible entry.

      @Suppress("UseWithIndex") // Avoid allocating an iterator.
      for (entryIndex in firstEntryIndex..lastEntryIndex) {

        // Draw a dot at the appropriate location.

        val entry = entries[entryIndex]
        val x = (columnWidth.toPx() / 2) + (columnWidth.toPx() * columnIndex) - listState.firstVisibleItemScrollOffset
        val y = entry.getGraphY(lineGraphHeight, scale).toPx()

        drawCircle(
          color = foregroundColour,
          radius = 3.dp.toPx(),
          center = Offset(x, y)
        )

        // If there's an entry prior to the current, draw a line between the previous
        // coordinates and the current coordinates.

        entries.getOrNull(entryIndex - 1)?.let { previousEntry ->
          val previousX = x - columnWidth.toPx()
          val previousY = previousEntry.getGraphY(lineGraphHeight, scale).toPx()
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

          drawLine(fadedColour, start, end, strokeWidth = 3.dp.toPx(), StrokeCap.Round)
        }

        columnIndex++
      }
    }
  }
}

private fun TimeForecastGraphItem.getGraphY(graphHeight: Dp, scale: TemperatureScale): Dp {
  val normalisedY = (temperatureC - scale.min) / scale.range.toFloat()
  return graphHeight - (graphHeight * normalisedY)
}

/**
 * Calculate how much the temperature fluctuates given a collection of graph items.
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
