package codes.chrishorner.socketweather.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.styles.LargeTempTextStyle
import codes.chrishorner.socketweather.styles.MediumTempTextStyle
import codes.chrishorner.socketweather.util.ThickDivider

@Composable
fun ForecastUi(forecast: Forecast) {

  val testConditions = FormattedConditions(
    iconDescriptor = "hazy",
    isNight = false,
    currentTemperature = "17°",
    highTemperature = "22°",
    lowTemperature = "14°",
    feelsLikeTemperature = "15.8°",
    description = "Partly cloudy. Areas of haze. Winds southerly 20 to 30 km/h decreasing to 15 to 20 km/h in the evening."
  )

  Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
    Observations(testConditions)

    Text(
      text = testConditions.description,
      style = MaterialTheme.typography.body1,
      modifier = Modifier.padding(horizontal = 16.dp)
    )

    ThickDivider(
      modifier = Modifier
        .padding(vertical = 16.dp)
        .fillMaxWidth()
    )

    TimeForecastGraph(
      entries = listOf(
        TimeForecastGraphItem(20, "20°", "8 AM", 0, ""),
        TimeForecastGraphItem(22, "22°", "11 AM", 10, "10%"),
        TimeForecastGraphItem(18, "18°", "1 PM", 20, "20%"),
        TimeForecastGraphItem(16, "16°", "4 PM", 80, "80%"),
        TimeForecastGraphItem(12, "12°", "7 PM", 70, "70%"),
        TimeForecastGraphItem(9, "9°", "10 PM", 20, "20%"),
        TimeForecastGraphItem(8, "8°", "1 AM", 0, ""),
        TimeForecastGraphItem(9, "9°", "4 AM", 0, ""),
        TimeForecastGraphItem(13, "13°", "7 AM", 0, ""),
        TimeForecastGraphItem(22, "22°", "10 AM", 0, ""),
      )
    )
  }
}

data class FormattedConditions(
  val iconDescriptor: String,
  val isNight: Boolean,
  val currentTemperature: String,
  val highTemperature: String,
  val lowTemperature: String,
  val feelsLikeTemperature: String,
  val description: String,
)

@Composable
private fun Observations(conditions: FormattedConditions) {
  Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

    Row(verticalAlignment = Alignment.CenterVertically) {
      Image(
        painter = painterResource(weatherIconRes(conditions.iconDescriptor, conditions.isNight)),
        contentDescription = stringResource(R.string.home_currentIconDesc),
        colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground),
        modifier = Modifier.size(72.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = conditions.currentTemperature,
        style = LargeTempTextStyle,
      )
    }

    Column(horizontalAlignment = Alignment.End) {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = conditions.highTemperature,
        style = MediumTempTextStyle,
      )
      Spacer(modifier = Modifier.height(12.dp))
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(28.dp)
          .offset(x = (-16).dp)
          .clip(RoundedCornerShape(2.dp))
          .background(MaterialTheme.colors.onBackground.copy(alpha = 0.2f))
      )
      Spacer(modifier = Modifier.height(12.dp))
      Row {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          Text(
            text = stringResource(R.string.home_feelsLike),
            style = MaterialTheme.typography.h5,
            modifier = Modifier.alignByBaseline()
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = conditions.feelsLikeTemperature,
          style = MediumTempTextStyle,
          modifier = Modifier.alignByBaseline()
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
          text = conditions.lowTemperature,
          style = MediumTempTextStyle,
          modifier = Modifier.alignByBaseline()
        )
      }
    }
  }
}

@DrawableRes
private fun weatherIconRes(descriptor: String, night: Boolean): Int = when (descriptor) {
  "sunny" -> R.drawable.ic_weather_sunny_24dp
  "clear" -> if (night) R.drawable.ic_weather_clear_night_24dp else R.drawable.ic_weather_sunny_24dp
  "mostly_sunny", "partly_cloudy" -> if (night) R.drawable.ic_weather_partly_cloudy_night_24dp else R.drawable.ic_weather_partly_cloudy_24dp
  "cloudy" -> R.drawable.ic_weather_cloudy_24dp
  "hazy" -> if (night) R.drawable.ic_weather_hazy_night_24dp else R.drawable.ic_weather_hazy_24dp
  "light_rain", "light_shower" -> R.drawable.ic_weather_light_rain_24dp
  "windy" -> R.drawable.ic_weather_windy_24dp
  "fog" -> R.drawable.ic_weather_fog_24dp
  "shower", "rain", "heavy_shower" -> R.drawable.ic_weather_rain_24dp
  "dusty" -> R.drawable.ic_weather_dusty_24dp
  "frost" -> R.drawable.ic_weather_frost_24dp
  "snow" -> R.drawable.ic_weather_snow_24dp
  "storm" -> R.drawable.ic_weather_storm_24dp
  "cyclone" -> R.drawable.ic_weather_cyclone_24dp
  else -> R.drawable.ic_weather_unknown_24dp
}
