package codes.chrishorner.socketweather.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.ForecastLoader
import codes.chrishorner.socketweather.data.ForecastLoader.State.Error
import codes.chrishorner.socketweather.data.ForecastLoader.State.FindingLocation
import codes.chrishorner.socketweather.data.ForecastLoader.State.Idle
import codes.chrishorner.socketweather.data.ForecastLoader.State.LoadingForecast
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.LocationSelection.FollowMe
import codes.chrishorner.socketweather.data.LocationSelection.None
import codes.chrishorner.socketweather.data.LocationSelection.Static
import codes.chrishorner.socketweather.data.Store
import codes.chrishorner.socketweather.util.MoleculeScreenModel
import codes.chrishorner.socketweather.util.Strings
import codes.chrishorner.socketweather.util.localTimeAtZone
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HomeScreenModel(
  private val forecastLoader: ForecastLoader,
  private val forecast: StateFlow<Forecast?>,
  private val currentSelectionStore: Store<LocationSelection>,
  private val allSelections: StateFlow<Set<LocationSelection>>,
  private val strings: Strings,
  private val clock: Clock = Clock.systemDefaultZone(),
) : MoleculeScreenModel<HomeEvent, HomeState> {

  private val timeFormatter = DateTimeFormatter.ofPattern("h a")

  @Composable
  override fun states(events: Flow<HomeEvent>): HomeState {

    var currentInstant by remember { mutableStateOf(clock.instant()) }
    val forecast by forecast.collectAsState()
    val loadingState by remember { forecastLoader.states }.collectAsState()
    val currentSelection by remember { currentSelectionStore.data }.collectAsState()
    val allSelections by allSelections.collectAsState()
    val otherSelections = allSelections.minus(currentSelection).map { it.toLocationEntry() }

    LaunchedEffect(Unit) {
      forecastLoader.refreshIfNecessary()
      // Update the current time used for UI labels every 10 seconds.
      while (true) {
        delay(10_000)
        currentInstant = clock.instant()
      }
    }

    return HomeState(
      toolbarTitle = getToolbarTitle(loadingState, forecast),
      toolbarSubtitle = getToolbarSubtitle(loadingState, forecast, currentInstant),
      currentLocation = currentSelection.toLocationEntry(),
      savedLocations = otherSelections,
      content = getContent(loadingState, forecast),
      showRefreshingIndicator = shouldShowRefreshingIndicator(loadingState, forecast),
    )
  }

  private fun getToolbarTitle(
    loadingState: ForecastLoader.State,
    forecast: Forecast?,
  ): String = when (loadingState) {
    Idle, LoadingForecast, is Error -> forecast?.location?.name ?: strings[R.string.home_loading]
    FindingLocation -> strings[R.string.home_findingLocation]
  }

  private fun getToolbarSubtitle(
    loadingState: ForecastLoader.State,
    forecast: Forecast?,
    currentInstant: Instant,
  ): String? = when (loadingState) {
    LoadingForecast, FindingLocation -> strings[R.string.home_updatingNow]
    Idle, is Error -> {
      val lastUpdate = forecast?.updateTime
      if (lastUpdate != null) {
        if (Duration.between(lastUpdate, currentInstant).toMinutes() > 0) {
          strings.get(R.string.home_lastUpdated, strings.getRelativeTimeSpanString(lastUpdate))
        } else {
          strings[R.string.home_justUpdated]
        }
      } else {
        null
      }
    }
  }

  private fun getContent(
    loadingState: ForecastLoader.State,
    forecast: Forecast?,
  ): HomeState.Content = when (loadingState) {
    Idle ->
      forecast?.let { HomeState.Content.Loaded(it.format()) } ?: HomeState.Content.Empty
    FindingLocation, LoadingForecast ->
      forecast?.let { HomeState.Content.Loaded(it.format()) } ?: HomeState.Content.Loading
    is Error ->
      HomeState.Content.Error(loadingState.type)
  }

  private fun shouldShowRefreshingIndicator(loadingState: ForecastLoader.State, forecast: Forecast?): Boolean {
    return forecast != null && (loadingState == FindingLocation || loadingState == LoadingForecast)
  }

  private fun Forecast.format(): FormattedConditions {

    val graphItems = hourlyForecasts.map { hourlyForecast ->
      TimeForecastGraphItem(
        temperatureC = hourlyForecast.temp,
        formattedTemperature = strings.formatDegrees(hourlyForecast.temp),
        time = timeFormatter.format(hourlyForecast.time.atZone(location.timezone)).uppercase(Locale.getDefault()),
        rainChancePercent = hourlyForecast.rain.chance,
        formattedRainChance = strings.formatPercent(hourlyForecast.rain.chance),
      )
    }

    val currentDate = LocalDate.now(clock.withZone(location.timezone))
    val upcomingForecasts = upcomingForecasts.map { dateForecast ->
      val date = dateForecast.date.atZone(location.timezone)
      val dayText = if (date.toLocalDate() == currentDate.plusDays(1)) {
        strings[R.string.home_dateForecastTomorrow]
      } else {
        date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
      }

      return@map UpcomingForecast(
        day = dayText,
        percentChanceOfRain = dateForecast.rain.chance,
        formattedChanceOfRain = strings.formatPercent(dateForecast.rain.chance),
        iconDescriptor = dateForecast.icon_descriptor,
        lowTemperature = dateForecast.temp_min?.let { strings.formatDegrees(it) } ?: "--",
        highTemperature = strings.formatDegrees(dateForecast.temp_max)
      )
    }

    val uvWarningTimes = todayForecast.uv.run {
      if (start_time == null || end_time == null) {
        null
      } else {
        strings.get(
          R.string.home_uvProtection,
          start_time.localTimeAtZone(location.timezone),
          end_time.localTimeAtZone(location.timezone)
        )
      }
    }

    return FormattedConditions(
      iconDescriptor = iconDescriptor,
      isNight = night,
      currentTemperature = strings.formatDegrees(currentTemp),
      highTemperature = strings.formatDegrees(highTemp),
      lowTemperature = strings.formatDegrees(lowTemp),
      feelsLikeTemperature = tempFeelsLike?.let { strings.formatDegrees(it) } ?: "--",
      humidityPercent = humidity?.let { strings.formatPercent(it) },
      windSpeed = strings.get(R.string.home_wind, wind.speed_kilometre),
      uvWarningTimes = uvWarningTimes,
      description = todayForecast.run {
        val short = short_text?.takeIf { it.isNotBlank() }
        val extended = extended_text?.takeIf { it.isNotBlank() }
        if (short == null && extended == null) return@run null
        FormattedConditions.Description(
          short = short ?: extended,
          extended = extended ?: short,
          hasExtended = short != null && extended != null && short != extended
        )
      },
      graphItems = graphItems,
      upcomingForecasts = upcomingForecasts
    )
  }

  private fun LocationSelection.toLocationEntry(): LocationEntry = when (this) {
    FollowMe -> LocationEntry(
      selection = this,
      title = strings[R.string.switchLocation_followMeTitle],
      subtitle = strings[R.string.switchLocation_followMeSubtitle],
      showTrackingIcon = true
    )
    is Static -> LocationEntry(
      selection = this,
      title = location.name,
      subtitle = location.state
    )
    None -> LocationEntry(
      selection = this,
      title = strings[R.string.switchLocation_noneTitle],
      subtitle = strings[R.string.switchLocation_noneSubtitle]
    )
  }
}