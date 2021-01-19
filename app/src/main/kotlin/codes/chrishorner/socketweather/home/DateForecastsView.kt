package codes.chrishorner.socketweather.home

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.util.formatAsDegrees
import codes.chrishorner.socketweather.util.formatAsPercent
import codes.chrishorner.socketweather.util.getWeatherIconFor
import codes.chrishorner.socketweather.util.inflate
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.TextStyle
import java.util.Locale

class DateForecastsView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

  private var currentForecast: Forecast? = null

  init {
    orientation = VERTICAL
  }

  fun display(forecast: Forecast) {
    if (forecast == currentForecast) return

    removeAllViews()

    for (dateForecast in forecast.upcomingForecasts) {
      val view: View = inflate(R.layout.date_forecast_item)
      val title: TextView = view.findViewById(R.id.dateForecastItem_title)
      val icon: ImageView = view.findViewById(R.id.dateForecastItem_icon)
      val highTemp: TextView = view.findViewById(R.id.dateForecastItem_highTemp)
      val lowTemp: TextView = view.findViewById(R.id.dateForecastItem_lowTemp)

      // TODO: Consider moving this formatting into some other structure.
      val forecastDate: ZonedDateTime = dateForecast.date.atZone(forecast.location.timezone)
      val currentDate = LocalDate.now(forecast.location.timezone)
      val titleText = if (forecastDate.toLocalDate() == currentDate.plusDays(1)) {
        resources.getString(R.string.home_dateForecastTomorrow)
      } else {
        forecastDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
      }

      if (dateForecast.rain.chance > 0 && dateForecast.rain.amount.max ?: 0f >= 1f) {
        val rainChance: TextView = view.findViewById(R.id.dateForecastItem_rainChance)
        rainChance.isVisible = true
        rainChance.text = dateForecast.rain.chance.formatAsPercent(context)
      }

      title.text = titleText
      icon.setImageDrawable(context.getWeatherIconFor(dateForecast.icon_descriptor, false))
      highTemp.text = dateForecast.temp_max.formatAsDegrees(context)
      lowTemp.text = dateForecast.temp_min?.formatAsDegrees(context) ?: "--"
      addView(view)
    }

    currentForecast = forecast
  }
}
