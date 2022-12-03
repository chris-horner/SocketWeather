package codes.chrishorner.socketweather.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.styles.smallTemp
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * The minimum number of degrees to show on the graph. This means that if the temperature
 * isn't changing much over time, (unheard of in Melbourne), then the line graph won't
 * fluctuate too aggressively for small changes.
 */
private const val MinRangeOfDegrees = 8

private val TotalComponentHeight = 208.dp // Amount of vertical space this Composable wants to take up.
private val TopTitleSectionHeight = 32.dp // Height of area where we show the time.
private val BottomTitleSectionHeight = 32.dp // Height of area where we show chance of rain.
private val EntryColumnWidth = 64.dp // Amount of horizontal space given to each entry in the graph.
private val LinePointGap = 12.dp // Visual space between dots and lines on the graph.
private val GraphVerticalPadding = 16.dp // Amount of space between the title sections and the line graph.

// Total available space in which to draw the line graph.
private val GraphHeight = TotalComponentHeight - TopTitleSectionHeight - BottomTitleSectionHeight

private data class TemperatureScale(val min: Int, val max: Int) {
  val range = abs(min - max)
}

@Composable
fun TimeForecastGraph(modifier: Modifier = Modifier, entries: List<TimeForecastGraphItem>) {
  // Total height available for the line graph to render. This should be shorter than GraphHeight,
  // allowing the temperature text to be rendered above each dot.
  val lineGraphHeight: Dp
  // Rough approximation of how tall the temperature text will be.
  val temperatureTextHeight: Dp

  val scale = getTemperatureScale(entries)
  val listState = rememberLazyListState()
  val headerFooterColor = MaterialTheme.colorScheme.surfaceVariant

  with(LocalDensity.current) {
    // Add 8dp to approximate the actual height.
    temperatureTextHeight = MaterialTheme.typography.smallTemp.fontSize.toDp() + 8.dp
    lineGraphHeight = GraphHeight - temperatureTextHeight - (GraphVerticalPadding * 2)
  }

  Box(modifier = modifier.requiredHeight(TotalComponentHeight)) {

    // Start by drawing some background colour to sit behind the time and chance of rain.
    TitleBackgrounds(headerFooterColor)

    // Then draw time, temperature, and chance of rain as scrollable LazyRow items.
    ScrollableEntries(lineGraphHeight, scale, entries, listState)

    // Next, inspect the scrolling list state and determine where and what to draw
    // for the line graph.
    LineGraph(
      lineGraphHeight, scale, entries, listState,
      modifier = Modifier
        .fillMaxWidth()
        .height(lineGraphHeight)
        .offset(y = TopTitleSectionHeight + temperatureTextHeight + GraphVerticalPadding)
    )

    // Finally, render some icons to sit on top of the title sections.
    TitleIcons(headerFooterColor)
  }
}

/**
 * For each entry, draw the time, temperature, and chance of rain in a horizontal scrolling list.
 */
@Composable
private fun ScrollableEntries(
  lineGraphHeight: Dp,
  scale: TemperatureScale,
  entries: List<TimeForecastGraphItem>,
  listState: LazyListState,
) {
  LazyRow(
    state = listState,
    modifier = Modifier.fillMaxHeight()
  ) {
    itemsIndexed(entries) { index, entry ->
      Column(
        modifier = Modifier
          .width(EntryColumnWidth)
          .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {

        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.height(TopTitleSectionHeight)
        ) {
          // Don't draw a time title for the first entry.
          if (index > 0) {
            Text(
              text = entry.time,
              style = MaterialTheme.typography.labelMedium,
            )
          }
        }

        Box(
          modifier = Modifier
            .height(GraphHeight)
            .padding(vertical = GraphVerticalPadding)
        ) {
          // The position of the temperature text is dictated by where it should sit
          // on the graph, minus some amount to make it appear above the dot.
          val yOffset = entry.getGraphY(lineGraphHeight, scale) - 6.dp
          Text(
            text = entry.formattedTemperature,
            style = MaterialTheme.typography.smallTemp,
            modifier = Modifier.offset(y = yOffset),
          )
        }

        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.height(TopTitleSectionHeight)
        ) {
          // Don't draw a chance of rain title for the first entry, or any entries
          // where the chance of rain is 0.
          if (entry.rainChancePercent > 0 && index > 0) {
            Text(
              text = entry.formattedRainChance,
              style = MaterialTheme.typography.labelSmall,
            )
          }
        }
      }
    }
  }
}

/**
 * Uses the [LazyListState] to determine which entries are currently visible
 * and draws a line graph.
 */
@Composable
private fun LineGraph(
  lineGraphHeight: Dp,
  scale: TemperatureScale,
  entries: List<TimeForecastGraphItem>,
  listState: LazyListState,
  modifier: Modifier,
) {
  val dotColor = MaterialTheme.colorScheme.onBackground
  val lineColor = remember { dotColor.copy(alpha = 0.2f) }

  Canvas(modifier) {

    // Determine which entries are currently on screen.

    val firstEntryIndex = listState.firstVisibleItemIndex
    val lastEntryIndex = (firstEntryIndex + (size.width / EntryColumnWidth.toPx()).toInt() + 1)
      .coerceAtMost(entries.size - 1)

    var columnIndex = 0

    // Iterate through every currently visible entry.

    @Suppress("UseWithIndex") // Avoid allocating an iterator.
    for (entryIndex in firstEntryIndex..lastEntryIndex) {

      // Draw a dot at the appropriate location.

      val entry = entries[entryIndex]
      val x =
        (EntryColumnWidth.toPx() / 2) + (EntryColumnWidth.toPx() * columnIndex) - listState.firstVisibleItemScrollOffset
      val y = entry.getGraphY(lineGraphHeight, scale).toPx()

      drawCircle(
        color = dotColor,
        radius = 3.dp.toPx(),
        center = Offset(x, y)
      )

      // If there's an entry prior to the current, draw a line between the previous
      // coordinates and the current coordinates.

      entries.getOrNull(entryIndex - 1)?.let { previousEntry ->
        val previousX = x - EntryColumnWidth.toPx()
        val previousY = previousEntry.getGraphY(lineGraphHeight, scale).toPx()
        val slope = (y - previousY) / (x - previousX)
        val start: Offset
        val end: Offset

        if (slope == 0f) {
          start = Offset(x = previousX + LinePointGap.toPx(), y = previousY)
          end = Offset(x = x - LinePointGap.toPx(), y = previousY)
        } else {
          val dx = LinePointGap.toPx() / sqrt(1 + (slope * slope))
          val dy = slope * dx
          start = Offset(x = previousX + dx, y = previousY + dy)
          end = Offset(x = x - dx, y = y - dy)
        }

        drawLine(lineColor, start, end, strokeWidth = 3.dp.toPx(), StrokeCap.Round)
      }

      columnIndex++
    }
  }
}

/**
 * The slightly different coloured bars that sit on the top and bottom of the graph.
 */
@Composable
private fun TitleBackgrounds(backgroundColor: Color) {
  Column(
    verticalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier.fillMaxSize(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(TopTitleSectionHeight)
        .background(backgroundColor)
    )
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(BottomTitleSectionHeight)
        .background(backgroundColor)
    )
  }
}

/**
 * The icons for time and chance of rain that sit at the start of the title section.
 */
@Composable
private fun TitleIcons(backgroundColor: Color) {
  val gradientBackground = Brush.horizontalGradient(
    0f to backgroundColor,
    0.7f to backgroundColor,
    1f to backgroundColor.copy(alpha = 0f),
  )

  CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
    Column(
      verticalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxHeight(),
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .width(EntryColumnWidth)
          .height(TopTitleSectionHeight)
          .background(gradientBackground)
      ) {
        Icon(Icons.Default.Schedule, contentDescription = null)
      }
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .width(EntryColumnWidth)
          .height(BottomTitleSectionHeight)
          .background(gradientBackground)
      ) {
        Icon(painterResource(R.drawable.ic_rain_chance_24dp), contentDescription = null)
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
  // then force the min and max temperatures to adhere to MinRangeOfDegrees.
  if (scale < MinRangeOfDegrees) {
    val tempDelta = (MinRangeOfDegrees - scale) / 2
    min -= tempDelta
    max += tempDelta
  }

  return TemperatureScale(min, max)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
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
