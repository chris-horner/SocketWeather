package codes.chrishorner.socketweather.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.R.string
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.styles.LargeTempTextStyle
import codes.chrishorner.socketweather.styles.MediumTempTextStyle
import codes.chrishorner.socketweather.util.ThickDivider
import codes.chrishorner.socketweather.util.formatAsDegrees

@Composable
fun ForecastUi(forecast: Forecast) {
  Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
    Observations(forecast)

    ThickDivider(
      modifier = Modifier
        .padding(horizontal = 16.dp)
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
      )
    )
  }
}

@Composable
private fun Observations(forecast: Forecast) {
  ConstraintLayout(
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .padding(top = 8.dp, bottom = 16.dp)
      .fillMaxWidth()
  ) {
    val (icon, currentTemp, feelsLikeTitle, feelsLikeTemp, highTemp, lowTemp, tempLine, description) = createRefs()

    Image(
      painter = painterResource(weatherIconRes(forecast.iconDescriptor, forecast.night)),
      contentDescription = stringResource(string.home_currentIconDesc),
      colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground),
      modifier = Modifier
        .size(72.dp)
        .constrainAs(icon) {
          start.linkTo(parent.start)
          top.linkTo(parent.top)
          bottom.linkTo(currentTemp.bottom)
        }
    )

    Text(
      text = forecast.currentTemp.formatAsDegrees(),
      style = LargeTempTextStyle,
      modifier = Modifier.constrainAs(currentTemp) {
        start.linkTo(icon.end, margin = 12.dp)
        top.linkTo(parent.top)
      }
    )

    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
      Text(
        text = stringResource(string.home_feelsLike),
        style = MaterialTheme.typography.h5,
        modifier = Modifier.constrainAs(feelsLikeTitle) {
          start.linkTo(parent.start)
          top.linkTo(currentTemp.bottom, margin = 16.dp)
        }
      )
    }

    Text(
      text = forecast.tempFeelsLike?.formatAsDegrees() ?: "--",
      style = MediumTempTextStyle,
      modifier = Modifier.constrainAs(feelsLikeTemp) {
        start.linkTo(feelsLikeTitle.end, margin = 8.dp)
        baseline.linkTo(feelsLikeTitle.baseline)
      }
    )

    Text(
      text = forecast.highTemp.formatAsDegrees(),
      style = MediumTempTextStyle,
      modifier = Modifier.constrainAs(highTemp) {
        end.linkTo(parent.end)
        top.linkTo(parent.top, margin = 12.dp)
      }
    )

    Text(
      text = forecast.lowTemp.formatAsDegrees(),
      style = MediumTempTextStyle,
      modifier = Modifier.constrainAs(lowTemp) {
        end.linkTo(parent.end)
        baseline.linkTo(feelsLikeTemp.baseline)
      }
    )

    Box(
      modifier = Modifier
        .width(4.dp)
        .constrainAs(tempLine) {
          centerHorizontallyTo(highTemp)
          top.linkTo(highTemp.bottom, margin = 12.dp)
          bottom.linkTo(lowTemp.top, margin = 12.dp)
          height = Dimension.fillToConstraints
        }
        .clip(RoundedCornerShape(2.dp))
        .background(MaterialTheme.colors.onBackground.copy(alpha = 0.2f))
    )

    val descriptionText = forecast.todayForecast.extended_text ?: forecast.todayForecast.short_text
    if (descriptionText != null) {
      Text(
        text = descriptionText,
        modifier = Modifier
          .constrainAs(description) {
            top.linkTo(feelsLikeTitle.bottom, margin = 16.dp)
          }
          .fillMaxWidth()
      )
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
