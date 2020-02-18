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
import codes.chrishorner.socketweather.data.ThreeHourlyForecast
import codes.chrishorner.socketweather.util.dpToPx
import codes.chrishorner.socketweather.util.getThemeColour
import codes.chrishorner.socketweather.util.getWeatherIconFor
import codes.chrishorner.socketweather.util.textHeight
import org.threeten.bp.ZoneId
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
  private var icons: List<Drawable> = emptyList()

  private var scale = MIN_SCALE_OF_DEGREES
  private var minTemp = 0
  private var maxTemp = 0

  private val columnWidth = dpToPx(56)
  private val timeTextBaselineInset = dpToPx(8)
  private val iconSize = dpToPx(24)
  private val iconTemperatureGap = dpToPx(2)
  private val timeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val temperatureTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  private val verticalPadding = dpToPx(16)
  private val timeFormatter = DateTimeFormatter.ofPattern("h a")

  init {
    val textAttributes = intArrayOf(android.R.attr.textSize, android.R.attr.textColor, R.attr.fontFamily)

    context.withStyledAttributes(R.style.TextAppearance_SocketWeather_Caption, textAttributes) {
      timeTextPaint.textSize = getDimensionPixelSizeOrThrow(0).toFloat()
      timeTextPaint.color = getColorOrThrow(1)
      timeTextPaint.typeface = getFontOrThrow(2)
      timeTextPaint.textAlign = Paint.Align.CENTER
    }

    context.withStyledAttributes(R.style.TextAppearance_SocketWeather_Body2, textAttributes) {
      temperatureTextPaint.textSize = getDimensionPixelSizeOrThrow(0).toFloat()
      temperatureTextPaint.color = getColorOrThrow(1)
      temperatureTextPaint.typeface = getFontOrThrow(2)
      temperatureTextPaint.textAlign = Paint.Align.CENTER
    }
  }

  fun display(forecasts: List<ThreeHourlyForecast>) {
    if (forecasts == this.forecasts || forecasts.isEmpty()) return

    this.forecasts = forecasts
    timeTexts = forecasts.map { timeFormatter.format(it.time.atZone(ZoneId.systemDefault())) }
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

    val graphHeight =
        height - (verticalPadding * 2) - timeTextPaint.textHeight().toInt() - iconSize - temperatureTextPaint.textHeight().toInt() - iconTemperatureGap

    for (index in forecasts.indices) {
      val columnX = index * columnWidth

      val textX = columnX + (columnWidth / 2f)
      val timeTextY = (height - timeTextBaselineInset).toFloat()
      canvas.drawText(timeTexts[index], textX, timeTextY, timeTextPaint)

      val iconX = columnX + ((columnWidth - iconSize) / 2)
      val normalisedY = (forecasts[index].temp - minTemp) / scale.toFloat()
      val positionY = (verticalPadding + graphHeight) - (graphHeight * normalisedY).toInt()
      val iconY = positionY - (iconSize / 2)
      val icon = icons[index]
      icon.setBounds(iconX, iconY, iconX + iconSize, iconY + iconSize)
      icon.draw(canvas)

      val temperatureTextY = iconY + iconSize + iconTemperatureGap + temperatureTextPaint.textHeight()
      canvas.drawText(forecasts[index].temp.toString(), textX, temperatureTextY, temperatureTextPaint)
    }
  }
}
