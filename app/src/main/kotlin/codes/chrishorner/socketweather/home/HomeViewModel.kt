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
import codes.chrishorner.socketweather.home.HomeEvent.Refresh
import codes.chrishorner.socketweather.home.HomeEvent.SwitchLocation
import codes.chrishorner.socketweather.util.Strings
import codes.chrishorner.socketweather.util.localTimeAtZone
import codes.chrishorner.socketweather.util.tickerFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
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

  val states: StateFlow<HomeState> = tickerFlow(10_000, emitImmediately = true)
    .flatMapLatest { forecaster.states }
    .combine(locationStore.savedSelections) { forecasterState, savedSelections ->
      forecasterState.toHomeState(savedSelections)
    }
    .stateIn(
      scope = scope,
      started = SharingStarted.Eagerly,
      initialValue = forecaster.states.value.toHomeState(locationStore.savedSelections.value)
    )

  fun handleEvent(event: HomeEvent) = when (event) {
    Refresh -> forecaster.refresh()
    is SwitchLocation -> switchLocation(event.selection)
    else -> error("Unhandled home event.")
  }

  private fun switchLocation(selection: LocationSelection) {
    scope.launch {
      locationStore.select(selection)

    }
  }

  private fun Forecaster.State.toHomeState(savedSelections: Set<LocationSelection>): HomeState {
    val toolbarTitle = when (this) {
      Forecaster.State.Idle -> strings[R.string.home_loading]
      is Forecaster.State.FindingLocation -> strings[R.string.home_findingLocation]
      is Forecaster.State.Refreshing -> this.previousForecast.location.name
      is Forecaster.State.Loaded -> this.forecast.location.name
      is Forecaster.State.Error -> when (val selection = this.selection) {
        is Static -> selection.location.name
        is FollowMe -> strings[R.string.home_findingLocation]
        is None -> throw IllegalStateException("Cannot display LocationSelection of None.")
      }
      else -> strings[R.string.home_loading]
    }

    val toolbarSubtitle = when (this) {
      is Forecaster.State.Refreshing -> strings[R.string.home_updatingNow]
      is Forecaster.State.Loaded -> {
        if (Duration.between(forecast.updateTime, clock.instant()).toMinutes() > 0) {
          strings.get(R.string.home_lastUpdated, strings.getRelativeTimeSpanString(forecast.updateTime))
        } else {
          strings[R.string.home_justUpdated]
        }
      }
      else -> null
    }

    val content = when (this) {
      Forecaster.State.Idle -> HomeState.Content.Empty
      is Forecaster.State.FindingLocation, is Forecaster.State.LoadingForecast -> HomeState.Content.Loading
      is Forecaster.State.Refreshing -> HomeState.Content.Refreshing(previousForecast.format())
      is Forecaster.State.Loaded -> HomeState.Content.Loaded(forecast.format())
      is Forecaster.State.Error -> HomeState.Content.Error(type)
    }

    return HomeState(
      toolbarTitle = toolbarTitle,
      toolbarSubtitle = toolbarSubtitle,
      currentLocation = selection.toLocationEntry(),
      savedLocations = savedSelections.minus(selection).map { it.toLocationEntry() },
      content = content,
    )
  }

  private fun Forecast.format(): FormattedConditions {

    val graphItems = hourlyForecasts.map { hourlyForecast ->
      TimeForecastGraphItem(
        temperatureC = hourlyForecast.temp,
        formattedTemperature = strings.formatDegrees(hourlyForecast.temp),
        time = timeFormatter.format(hourlyForecast.time.atZone(location.timezone)).toUpperCase(Locale.getDefault()),
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
      description = todayForecast.extended_text ?: todayForecast.short_text,
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
