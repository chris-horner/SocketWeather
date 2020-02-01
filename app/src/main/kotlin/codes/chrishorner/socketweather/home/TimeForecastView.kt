package codes.chrishorner.socketweather.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import codes.chrishorner.socketweather.data.ThreeHourlyForecast
import timber.log.Timber

class TimeForecastView(context: Context, attrs: AttributeSet) : ViewGroup(context, attrs) {

  private var forecasts: List<ThreeHourlyForecast> = emptyList()
  private var hourViews: List<TextView> = emptyList()
  private var temperatureViews: List<TextView> = emptyList()

  init {
    setWillNotDraw(false)
  }

  fun display(forecasts: List<ThreeHourlyForecast>) {
    if (forecasts == this.forecasts) return

  }

  override fun onMeasure(widthSpec: Int, heightSpec: Int) {
    val width = MeasureSpec.getSize(widthSpec)
    val height = MeasureSpec.getSize(heightSpec)

    setMeasuredDimension(width, height)
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
  }
}
