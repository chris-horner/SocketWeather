package codes.chrishorner.socketweather.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import androidx.core.content.withStyledAttributes
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.Rain
import codes.chrishorner.socketweather.data.ThreeHourlyForecast
import codes.chrishorner.socketweather.util.dpToPx
import codes.chrishorner.socketweather.util.formatAsDegrees
import codes.chrishorner.socketweather.util.getCompatFontOrThrow
import codes.chrishorner.socketweather.util.requireDrawable
import codes.chrishorner.socketweather.util.textHeight
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * The minimum number of degrees to show on the graph. This means that if the temperature
 * isn't changing much over time, (unheard of in Melbourne), then the line graph won't
 * fluctuate too aggressively for small changes.
 */
private const val MIN_SCALE_OF_DEGREES = 8

/**
 * Renders the list of [ThreeHourlyForecast] from a [Forecast] as a line graph showing
 * the change in temperature.
 *
 * Times are displayed relative to the timezone of the forecasts location, rather than
 * the user's timezone.
 */
class TimeForecastView(context: Context, attrs: AttributeSet) : View(context, attrs) {

  private var forecasts: List<ThreeHourlyForecast> = emptyList()
  private var timeTexts: List<String> = emptyList()
  private var rainChanceTexts: List<String> = emptyList()
  private var temperatureTexts: List<String> = emptyList()
  private var displayRainChance = false
  private var scale = MIN_SCALE_OF_DEGREES
  private var minTemp = 0
  private var maxTemp = 0

  private val columnWidth = dpToPx(64)
  private val temperatureGap = dpToPx(12)
  private val dotRadius = dpToPx(3f)
  private val graphTimeGap = dpToPx(8)
  private val verticalPadding = dpToPx(16)
  private val linePointGap = dpToPx(12)
  private val temperatureTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val timeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val rainTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val timeFormatter = DateTimeFormatter.ofPattern("h a")
  private val rainIcon = context.requireDrawable(R.drawable.ic_water)

  init {
    val textAttributes = intArrayOf(android.R.attr.textSize, android.R.attr.textColor, R.attr.fontFamily)

    context.withStyledAttributes(R.style.TextAppearance_SocketWeather_Overline, textAttributes) {
      timeTextPaint.textSize = getDimensionPixelSizeOrThrow(0).toFloat()
      timeTextPaint.color = getColorOrThrow(1)
      timeTextPaint.typeface = getCompatFontOrThrow(context, 2)
      timeTextPaint.textAlign = Paint.Align.CENTER
    }

    context.withStyledAttributes(R.style.TextAppearance_SocketWeather_Overline, textAttributes) {
      rainTextPaint.textSize = getDimensionPixelSizeOrThrow(0).toFloat()
      rainTextPaint.color = getColorOrThrow(1)
      rainTextPaint.typeface = getCompatFontOrThrow(context, 2)
    }

    context.withStyledAttributes(R.style.TextAppearance_SocketWeather_SmallTemp, textAttributes) {
      temperatureTextPaint.textSize = getDimensionPixelSizeOrThrow(0).toFloat()
      temperatureTextPaint.color = getColorOrThrow(1)
      temperatureTextPaint.typeface = getCompatFontOrThrow(context, 2)
      temperatureTextPaint.textAlign = Paint.Align.CENTER
    }

    linePaint.color = AppCompatResources.getColorStateList(context, R.color.color_on_background_08).defaultColor
    linePaint.style = Paint.Style.STROKE
    linePaint.strokeWidth = dpToPx(3f)
    linePaint.strokeCap = Paint.Cap.ROUND
  }

  fun display(forecast: Forecast) {
    if (forecast.hourlyForecasts == this.forecasts || forecast.hourlyForecasts.isEmpty()) return

    this.forecasts = forecast.hourlyForecasts
    timeTexts = forecasts.map {
      timeFormatter.format(it.time.atZone(forecast.location.timezone)).toUpperCase(Locale.getDefault())
    }
    temperatureTexts = forecasts.map { it.temp.formatAsDegrees(context) }
    rainChanceTexts = forecasts.map { it.rain.getPercentageString() }
    displayRainChance = rainChanceTexts.any { it.isNotEmpty() }

    minTemp = forecasts.map { it.temp }.minOrNull()!!
    maxTemp = forecasts.map { it.temp }.maxOrNull()!!
    scale = abs(maxTemp - minTemp)

    // Check the scale at which the temperature is changing. If it's lower than our minimum,
    // then force the min and max temperatures to adhere to MIN_SCALE_OF_DEGREES.
    if (scale < MIN_SCALE_OF_DEGREES) {
      val tempDelta = (MIN_SCALE_OF_DEGREES - scale) / 2
      minTemp -= tempDelta
      maxTemp += tempDelta
      scale = MIN_SCALE_OF_DEGREES
    }

    requestLayout()
    invalidate()
  }

  override fun onMeasure(widthSpec: Int, heightSpec: Int) {
    val width = forecasts.size * columnWidth
    val height = MeasureSpec.getSize(heightSpec)
    setMeasuredDimension(width, height)
  }

  override fun onDraw(canvas: Canvas) {

    // NOTE: If we ever want to animate components of the graph, it might be a good
    // idea to move these calculations into `display()` rather than recalculate
    // them every `onDraw()`.

    // Start by working out whether or not we'll be displaying rain chance at the
    // top of the graph, and how much space that will take.

    val rainTextHeight: Float
    val rainTotalHeight: Int

    if (displayRainChance) {
      rainTextHeight = abs(rainTextPaint.fontMetrics.ascent)
      rainTotalHeight = max(rainTextHeight.toInt(), rainIcon.intrinsicHeight)
    } else {
      rainTextHeight = 0f
      rainTotalHeight = 0
    }

    // Next calculate some values that will be constant over the graph.

    val timeTextHeight = timeTextPaint.textHeight()
    val temperatureTextHeight = temperatureTextPaint.textHeight()
    val graphHeight =
        height - timeTextHeight - rainTotalHeight - temperatureTextHeight - (dotRadius * 2) - temperatureGap - (verticalPadding * 2)
    val graphBottom = height - verticalPadding - timeTextHeight - graphTimeGap
    val timeTextBaseline = height - verticalPadding.toFloat()
    val rainTextBaseline = verticalPadding + ((rainTextHeight - rainTextHeight) / 2) + rainTextHeight + dpToPx(1)
    val rainIconTop = verticalPadding + ((rainTotalHeight - rainIcon.intrinsicHeight) / 2)

    // For each forecast, render a point on the graph, a temperature, a line,
    // and potentially the chance of rain.

    var previousX = -1f
    var previousY = -1f

    for (index in forecasts.indices) {
      val forecast = forecasts[index]
      val rainChanceText = rainChanceTexts[index]

      val columnX = index * columnWidth
      val positionX = columnX + (columnWidth / 2f)
      val normalisedY = (forecast.temp - minTemp) / scale.toFloat()
      val positionY = graphBottom - (graphHeight * normalisedY)
      val temperatureTextBaseline = positionY - temperatureGap

      // Render chance of rain.

      if (rainChanceText.isNotEmpty()) {
        val rainWidth = rainIcon.intrinsicWidth + rainTextPaint.measureText(rainChanceText).toInt()
        val rainX = positionX.toInt() - (rainWidth / 2)
        rainIcon.setBounds(rainX, rainIconTop, rainX + rainIcon.intrinsicWidth, rainIconTop + rainIcon.intrinsicHeight)
        rainIcon.draw(canvas)
        canvas.drawText(rainChanceText, rainIcon.bounds.right.toFloat(), rainTextBaseline, rainTextPaint)
      }

      // Render line between this point and the previous point, adding a little
      // bit of padding so the lines aren't visibly connected for _style_.

      if (previousX > 0f && previousY > 0f) {
        val slope = (positionY - previousY) / (positionX - previousX)
        val startX: Float
        val startY: Float
        val endX: Float
        val endY: Float

        if (slope == 0f) {
          startX = previousX + linePointGap
          startY = previousY
          endX = positionX - linePointGap
          endY = previousY
        } else {
          val dx = linePointGap / sqrt(1 + (slope * slope))
          val dy = slope * dx
          startX = previousX + dx
          startY = previousY + dy
          endX = positionX - dx
          endY = positionY - dy
        }

        canvas.drawLine(startX, startY, endX, endY, linePaint)
      }

      // Draw remaining components.

      canvas.drawCircle(positionX, positionY, dotRadius, temperatureTextPaint)
      canvas.drawText(temperatureTexts[index], positionX, temperatureTextBaseline, temperatureTextPaint)
      canvas.drawText(timeTexts[index], positionX, timeTextBaseline, timeTextPaint)

      previousX = positionX
      previousY = positionY
    }
  }

  private fun Rain.getPercentageString(): String {
    return if (chance > 0 && amount.max ?: 0f > 0.1f) "$chance%" else ""
  }
}
