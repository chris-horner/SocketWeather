package codes.chrishorner.socketweather.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.common.weatherIconRes
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import codes.chrishorner.socketweather.styles.largeTemp
import codes.chrishorner.socketweather.styles.mediumTemp
import codes.chrishorner.socketweather.styles.smallTemp

@Composable
fun ForecastUi(
  conditions: FormattedConditions,
  scrollState: ScrollState,
  modifier: Modifier = Modifier,
  onEvent: (HomeEvent) -> Unit = {}
) {

  Column(
    modifier = modifier
      .verticalScroll(scrollState)
      .testTag("forecast_scroll_container")
  ) {

    Spacer(modifier = Modifier.height(8.dp))

    Observations(conditions)

    Spacer(modifier = Modifier.height(16.dp))

    if (conditions.description != null) {
      Text(
        text = conditions.description,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(horizontal = 16.dp)
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    var showMoreSection by rememberSaveable { mutableStateOf(false) }

    AnimatedVisibility(visible = showMoreSection) {
      MoreSection(
        chanceOfRain = conditions.rainChance,
        humidity = conditions.humidityPercent,
        windSpeed = conditions.windSpeed,
        uvWarningTimes = conditions.uvWarningTimes,
      )
    }

    ButtonsSection(
      showingMore = showMoreSection,
      onRainRadarClick = { onEvent(HomeEvent.ViewRainRadar) },
      onMoreClick = { showMoreSection = !showMoreSection }
    )

    Spacer(modifier = Modifier.height(24.dp))

    TimeForecastGraph(
      entries = conditions.graphItems,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(16.dp))

    UpcomingForecasts(forecasts = conditions.upcomingForecasts)
  }
}

@Composable
private fun Observations(conditions: FormattedConditions) {
  Box(modifier = Modifier.padding(horizontal = 16.dp)) {

    Row(verticalAlignment = Alignment.CenterVertically) {
      Image(
        painter = painterResource(weatherIconRes(conditions.iconDescriptor, conditions.isNight)),
        contentDescription = stringResource(R.string.home_currentIconDesc),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
        modifier = Modifier.size(72.dp),
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = conditions.currentTemperature,
        style = MaterialTheme.typography.largeTemp,
      )
    }

    Column(horizontalAlignment = Alignment.End) {
      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = conditions.highTemperature,
        style = MaterialTheme.typography.mediumTemp,
      )
      Spacer(modifier = Modifier.height(12.dp))
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(28.dp)
          .offset(x = (-16).dp)
          .clip(RoundedCornerShape(2.dp))
          .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f))
      )
      Spacer(modifier = Modifier.height(12.dp))
      Row {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
          Text(
            text = stringResource(R.string.home_feelsLike),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.alignByBaseline()
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = conditions.feelsLikeTemperature,
          style = MaterialTheme.typography.mediumTemp,
          modifier = Modifier.alignByBaseline()
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
          text = conditions.lowTemperature,
          style = MaterialTheme.typography.mediumTemp,
          modifier = Modifier.alignByBaseline()
        )
      }
    }
  }
}

@Composable
private fun ButtonsSection(
  showingMore: Boolean,
  onRainRadarClick: () -> Unit,
  onMoreClick: () -> Unit,
) {

  val moreIconRotation: Float by animateFloatAsState(if (showingMore) 180f else 0f)

  Row(
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    ElevatedButton(
      onClick = onRainRadarClick,
      modifier = Modifier
        .testTag("rain_radar_button")
        .weight(1f)
        .heightIn(min = 48.dp)
    ) {
      Icon(Icons.Rounded.Radar, contentDescription = null)
      Spacer(modifier = Modifier.width(12.dp))
      Text(text = stringResource(R.string.home_rainRadarButton), maxLines = 1, overflow = Ellipsis)
    }
    ElevatedButton(
      onClick = onMoreClick,
      modifier = Modifier
        .testTag("more_button")
        .weight(1f)
        .heightIn(min = 48.dp)
    ) {
      Icon(
        imageVector = Icons.Rounded.ExpandMore,
        contentDescription = null,
        modifier = Modifier.rotate(moreIconRotation)
      )
      Spacer(modifier = Modifier.width(12.dp))
      Text(
        text = if (showingMore) stringResource(R.string.home_lessButton) else stringResource(R.string.home_moreButton),
        maxLines = 1,
        overflow = Ellipsis,
      )
    }
  }
}

@Composable
private fun MoreSection(
  chanceOfRain: String?,
  humidity: String?,
  windSpeed: String?,
  uvWarningTimes: String?,
) {
  Surface(
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.padding(16.dp)
    ) {
      if (chanceOfRain != null) {
        MoreSectionEntry(
          iconRes = R.drawable.ic_rain_chance_24dp,
          titleRes = R.string.home_rainChance,
          value = chanceOfRain,
        )
      }
      if (humidity != null) {
        MoreSectionEntry(
          iconRes = R.drawable.ic_water_24dp,
          titleRes = R.string.home_humidity,
          value = humidity,
        )
      }
      if (windSpeed != null) {
        MoreSectionEntry(
          iconRes = R.drawable.ic_weather_windy_24dp,
          titleRes = R.string.home_windSpeed,
          value = windSpeed,
        )
      }
      if (uvWarningTimes != null) {
        MoreSectionEntry(
          iconRes = R.drawable.ic_weather_sunny_alert_24dp,
          titleRes = R.string.home_uvWarningTimes,
          value = uvWarningTimes,
        )
      }
    }
  }
}

@Composable
private fun MoreSectionEntry(
  @DrawableRes iconRes: Int,
  @StringRes titleRes: Int,
  value: String,
) {
  Row {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
      Icon(painterResource(iconRes), contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = Ellipsis,
        modifier = Modifier.alignByBaseline(),
      )
    }
    Spacer(modifier = Modifier.weight(1f))
    Text(
      text = value,
      style = MaterialTheme.typography.smallTemp,
      maxLines = 1,
      modifier = Modifier.alignByBaseline(),
    )
  }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
private fun ForecastUiPreview() {
  SocketWeatherTheme {
    ForecastUi(
      scrollState = rememberScrollState(),
      conditions = FormattedConditions(
        iconDescriptor = "hazy",
        isNight = false,
        currentTemperature = "17°",
        highTemperature = "22°",
        lowTemperature = "14°",
        feelsLikeTemperature = "15.8°",
        humidityPercent = "50%",
        windSpeed = "20 km/h",
        uvWarningTimes = "10:00 - 16:00",
        rainChance = "10%",
        description = "Partly cloudy. Areas of haze. Winds southerly 20 to 30 km/h decreasing to 15 to 20 km/h in the evening.",
        graphItems = listOf(
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
        ),
        upcomingForecasts = listOf(
          UpcomingForecast("Tomorrow", 0, "", "partly_cloudy", "15°", "27°"),
          UpcomingForecast("Friday", 0, "", "partly_cloudy", "14°", "27°"),
          UpcomingForecast("Saturday", 50, "50%", "shower", "17°", "26°"),
        )
      )
    )
  }
}
