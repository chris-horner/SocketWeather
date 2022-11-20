package codes.chrishorner.socketweather.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.common.weatherIconRes
import codes.chrishorner.socketweather.styles.SmallTempTextStyle
import codes.chrishorner.socketweather.styles.SocketWeatherTheme

@Composable
fun UpcomingForecasts(forecasts: List<UpcomingForecast>) {
  Column(Modifier.padding(horizontal = 16.dp)) {
    for (forecast in forecasts) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.heightIn(56.dp).padding(vertical = 8.dp)
      ) {

        Text(
          text = forecast.day,
          style = MaterialTheme.typography.subtitle1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.weight(1f))

        if (forecast.percentChanceOfRain > 0) {
          CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
              text = forecast.formattedChanceOfRain,
              style = MaterialTheme.typography.subtitle2,
              modifier = Modifier.padding(end = 8.dp)
            )
          }
        }

        Icon(
          painter = painterResource(weatherIconRes(forecast.iconDescriptor)),
          contentDescription = null, // Not important for accessibility.
          modifier = Modifier.padding(end = 24.dp)
        )

        Text(
          text = forecast.lowTemperature,
          style = SmallTempTextStyle,
          textAlign = TextAlign.End,
          maxLines = 1,
          modifier = Modifier
            .widthIn(min = 36.dp)
            .padding(end = 8.dp)
        )

        Box(
          modifier = Modifier
            .width(24.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colors.onBackground.copy(alpha = 0.2f))
        )

        Text(
          text = forecast.highTemperature,
          style = SmallTempTextStyle,
          textAlign = TextAlign.End,
          modifier = Modifier.widthIn(min = 36.dp)
        )
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
fun PreviewUpcomingForecasts() {
  SocketWeatherTheme {
    UpcomingForecasts(
      listOf(
        UpcomingForecast("Tomorrow", 0, "", "partly_cloudy", "15°", "27°"),
        UpcomingForecast("Friday", 0, "", "partly_cloudy", "14°", "27°"),
        UpcomingForecast("Saturday", 50, "50%", "shower", "17°", "26°"),
      )
    )
  }
}
