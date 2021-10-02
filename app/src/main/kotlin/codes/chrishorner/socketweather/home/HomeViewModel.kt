package codes.chrishorner.socketweather.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.data.Forecast
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.LocationSelection.FollowMe
import codes.chrishorner.socketweather.data.LocationSelection.None
import codes.chrishorner.socketweather.data.LocationSelection.Static
import codes.chrishorner.socketweather.data.LocationSelectionStore
import codes.chrishorner.socketweather.home.FormattedConditions.Description
import codes.chrishorner.socketweather.home.HomeEvent.Refresh
import codes.chrishorner.socketweather.home.HomeEvent.SwitchLocation
import codes.chrishorner.socketweather.home.HomeEvent.ToggleDescription
import codes.chrishorner.socketweather.util.Strings
import codes.chrishorner.socketweather.util.localTimeAtZone
import codes.chrishorner.socketweather.util.tickerFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class HomeViewModel(
  private val forecaster: Forecaster,
  private val locationStore: LocationSelectionStore,
  private val strings: Strings,
  private val clock: Clock = Clock.systemDefaultZone(),
  overrideScope: CoroutineScope? = null
) : ViewModel() {

  private val timeFormatter = DateTimeFormatter.ofPattern("h a")
  private val scope = overrideScope ?: viewModelScope

  private val showExtendedDescription: MutableStateFlow<Boolean> = MutableStateFlow(false)

  val states: StateFlow<HomeState> = combine(
    tickerFlow(10_000, emitImmediately = true).flatMapLatest { forecaster.states },
    locationStore.savedSelections,
    showExtendedDescription
  ) { forecasterState, savedSelections, showExtendedDescription ->
    forecasterState.toHomeState(savedSelections, showExtendedDescription)
  }
    .stateIn(
      scope = scope,
      started = SharingStarted.Eagerly,
      initialValue = forecaster.states.value.toHomeState(
        locationStore.savedSelections.value,
        showExtendedDescription.value
      )
    )

  fun handleEvent(event: HomeEvent) = when (event) {
    Refresh -> forecaster.refresh()
    is SwitchLocation -> switchLocation(event.selection)
    is ToggleDescription -> toggleDescription(event.showExtended)
    else -> error("Unhandled home event.")
  }

  private fun switchLocation(selection: LocationSelection) {
    scope.launch {
      locationStore.select(selection)
    }
  }

  private fun toggleDescription(showExtended: Boolean) {
    scope.launch {
      showExtendedDescription.emit(showExtended)
    }
  }

  private fun Forecaster.LoadingState.toHomeState(
    savedSelections: Set<LocationSelection>,
    showExtendedDescription: Boolean
  ): HomeState {
    val toolbarTitle = when (this) {
      Forecaster.LoadingState.Idle -> strings[R.string.home_loading]
      is Forecaster.LoadingState.FindingLocation -> strings[R.string.home_findingLocation]
      is Forecaster.LoadingState.Refreshing -> this.previousForecast.location.name
      is Forecaster.LoadingState.Loaded -> this.forecast.location.name
      is Forecaster.LoadingState.Error -> when (val selection = this.selection) {
        is Static -> selection.location.name
        is FollowMe -> strings[R.string.home_findingLocation]
        is None -> throw IllegalStateException("Cannot display LocationSelection of None.")
      }
      else -> strings[R.string.home_loading]
    }

    val toolbarSubtitle = when (this) {
      is Forecaster.LoadingState.Refreshing -> strings[R.string.home_updatingNow]
      is Forecaster.LoadingState.Loaded -> {
        if (Duration.between(forecast.updateTime, clock.instant()).toMinutes() > 0) {
          strings.get(R.string.home_lastUpdated, strings.getRelativeTimeSpanString(forecast.updateTime))
        } else {
          strings[R.string.home_justUpdated]
        }
      }
      else -> null
    }

    val content = when (this) {
      Forecaster.LoadingState.Idle -> HomeState.Content.Empty
      is Forecaster.LoadingState.FindingLocation, is Forecaster.LoadingState.LoadingForecast -> HomeState.Content.Loading
      is Forecaster.LoadingState.Refreshing -> HomeState.Content.Refreshing(previousForecast.format(showExtendedDescription))
      is Forecaster.LoadingState.Loaded -> HomeState.Content.Loaded(forecast.format(showExtendedDescription))
      is Forecaster.LoadingState.Error -> HomeState.Content.Error(type)
    }

    return HomeState(
      toolbarTitle = toolbarTitle,
      toolbarSubtitle = toolbarSubtitle,
      currentLocation = selection.toLocationEntry(),
      savedLocations = savedSelections.minus(selection).map { it.toLocationEntry() },
      content = content,
    )
  }

  private fun Forecast.format(showExtendedDescription: Boolean): FormattedConditions {

    val graphItems = hourlyForecasts.map { hourlyForecast ->
      TimeForecastGraphItem(
        temperatureC = hourlyForecast.temp,
        formattedTemperature = strings.formatDegrees(hourlyForecast.temp),
        time = timeFormatter.format(hourlyForecast.time.atZone(location.timezone)).uppercase(Locale.getDefault()),
        rainChancePercent = hourlyForecast.rain.chance,
        formattedRainChance = strings.formatPercent(hourlyForecast.rain.chance),
      )
    }

    val currentDate = LocalDate.now(location.timezone)
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
        val hasExtendedDescription =
          !short_text.isNullOrBlank() && !extended_text.isNullOrBlank() && short_text != extended_text
        Description(
          text = when {
            hasExtendedDescription && showExtendedDescription -> extended_text!!
            hasExtendedDescription -> short_text!!
            else -> short_text ?: extended_text ?: return@run null
          },
          hasExtended = hasExtendedDescription,
          isExtended = showExtendedDescription
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
    None -> error("Empty location selection cannot have an entry.")
  }

  companion object {
    operator fun invoke(context: Context) = HomeViewModel(
      context.appSingletons.forecaster,
      context.appSingletons.locationSelectionStore,
      Strings.AndroidStrings(context)
    )
  }
}
