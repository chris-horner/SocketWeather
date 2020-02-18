package codes.chrishorner.socketweather.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import androidx.core.content.res.getFontOrThrow
import androidx.core.content.withStyledAttributes
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.Rain
import codes.chrishorner.socketweather.data.ThreeHourlyForecast
import codes.chrishorner.socketweather.util.dpToPx
import codes.chrishorner.socketweather.util.getThemeColour
import codes.chrishorner.socketweather.util.getWeatherIconFor
import codes.chrishorner.socketweather.util.textHeight
import org.threeten.bp.format.DateTimeFormatter
import kotlin.math.abs

/**
 * The minimum number of degrees to show on the graph. This means that if the temperature
 * isn't changing much over time, (unheard of in Melbourne), then the line graph won't
 * fluctuate too aggressively for small changes.
 */
private const val MIN_SCALE_OF_DEGREES = 8

class TimeForecastView(context: Context, attrs: AttributeSet) : View(context, attrs) {

  private var forecasts: List<ThreeHourlyForecast> = emptyList()
  private var timeTexts: List<String> = emptyList()
  private var rainChanceTexts: List<String> = emptyList()
  private var icons: List<Drawable> = emptyList()
  private var displayRainChance = false
  private var scale = MIN_SCALE_OF_DEGREES
  private var minTemp = 0
  private var maxTemp = 0

  private val columnWidth = dpToPx(56)
  private val timeTextBaselineInset = dpToPx(16)
  private val iconSize = dpToPx(24)
  private val iconTemperatureGap = dpToPx(2)
  private val temperatureTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val secondaryTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val verticalPadding = dpToPx(8)
  private val timeFormatter = DateTimeFormatter.ofPattern("h a")

  init {
    val textAttributes = intArrayOf(android.R.attr.textSize, android.R.attr.textColor, R.attr.fontFamily)

    context.withStyledAttributes(R.style.TextAppearance_SocketWeather_Caption, textAttributes) {
      secondaryTextPaint.textSize = getDimensionPixelSizeOrThrow(0).toFloat()
      secondaryTextPaint.color = getColorOrThrow(1)
      secondaryTextPaint.typeface = getFontOrThrow(2)
      secondaryTextPaint.textAlign = Paint.Align.CENTER
    }

    context.withStyledAttributes(R.style.TextAppearance_SocketWeather_Body2, textAttributes) {
      temperatureTextPaint.textSize = getDimensionPixelSizeOrThrow(0).toFloat()
      temperatureTextPaint.color = getColorOrThrow(1)
      temperatureTextPaint.typeface = getFontOrThrow(2)
      temperatureTextPaint.textAlign = Paint.Align.CENTER
    }
  }

  fun display(forecast: Forecast) {
    if (forecast.hourlyForecasts == this.forecasts || forecast.hourlyForecasts.isEmpty()) return

    this.forecasts = forecast.hourlyForecasts
    timeTexts = forecasts.map { timeFormatter.format(it.time.atZone(forecast.location.timezone)) }
    rainChanceTexts = forecasts.map { it.rain.getPercentageString() }
    displayRainChance = rainChanceTexts.any { it.isNotEmpty() }
    icons = forecasts
        .map { context.getWeatherIconFor(it.icon_descriptor, it.is_night).mutate() }
        .onEach { it.setTint(context.getThemeColour(android.R.attr.textColorPrimary)) }

    minTemp = forecasts.map { it.temp }.min()!!
    maxTemp = forecasts.map { it.temp }.max()!!
    scale = abs(maxTemp - minTemp)

    if (scale < MIN_SCALE_OF_DEGREES) {
      val tempDelta = (MIN_SCALE_OF_DEGREES - scale) / 2
      minTemp -= tempDelta
      maxTemp += tempDelta
      scale = MIN_SCALE_OF_DEGREES
    }

    requestLayout()
  }

  override fun onMeasure(widthSpec: Int, heightSpec: Int) {
    val width = forecasts.size * columnWidth
    val height = MeasureSpec.getSize(heightSpec)

    setMeasuredDimension(width, height)
  }

  override fun onDraw(canvas: Canvas) {

    val rainChanceHeight = if (displayRainChance) secondaryTextPaint.textHeight().toInt() else 0
    val graphHeight =
        height - (verticalPadding * 2) - rainChanceHeight - secondaryTextPaint.textHeight().toInt() - iconSize - temperatureTextPaint.textHeight().toInt() - iconTemperatureGap

    for (index in forecasts.indices) {
      val forecast = forecasts[index]
      val columnX = index * columnWidth
      val textX = columnX + (columnWidth / 2f)

      val timeTextY = (height - timeTextBaselineInset).toFloat()
      canvas.drawText(timeTexts[index], textX, timeTextY, secondaryTextPaint)

      val iconX = columnX + ((columnWidth - iconSize) / 2)
      val normalisedY = (forecast.temp - minTemp) / scale.toFloat()
      val positionY = (verticalPadding + graphHeight + rainChanceHeight) - (graphHeight * normalisedY).toInt()
      val iconY = positionY - (iconSize / 2)
      val icon = icons[index]
      icon.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
      icon.draw(canvas)

      val temperatureTextY = iconY + iconSize + iconTemperatureGap + temperatureTextPaint.textHeight()
      canvas.drawText(forecast.temp.toString(), textX, temperatureTextY, temperatureTextPaint)

      if (rainChanceTexts[index].isNotEmpty()) {
        val chanceTextY = iconY.toFloat() - dpToPx(6)
        canvas.drawText(rainChanceTexts[index], textX, chanceTextY, secondaryTextPaint)
      }
    }
  }

  private fun Rain.getPercentageString(): String {
    return if (chance > 0 && amount.min != null && amount.min > 0) "$chance%" else ""
  }
}
