package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.data.DeviceLocation
import codes.chrishorner.socketweather.data.Forecasts
import codes.chrishorner.socketweather.data.Location
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.WeatherApi
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.Loading
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.LocationFailed
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.NetworkFailed
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.Success
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scanReduce
import timber.log.Timber

class HomeViewModel(
    private val api: WeatherApi,
    currentSelectionUpdates: Flow<LocationSelection>,
    deviceLocationUpdates: Flow<DeviceLocation>
) {

  private val scope = MainScope()
  private val refreshChannel = BroadcastChannel<Unit>(1)
  private val statesChannel = ConflatedBroadcastChannel<State>()
  // Whether or not a subscription to device location updates should be maintained.
  private val locationUpdatesToggleChannel = ConflatedBroadcastChannel(false)

  init {

    val followMeUpdates: Flow<Location> = locationUpdatesToggleChannel.asFlow()
        .flatMapLatest { enabled -> if (enabled) deviceLocationUpdates else emptyFlow() }
        .distinctUntilChanged()
        .mapLatest { api.searchForLocation("${it.latitude},${it.longitude}") }
        .map { api.getLocation(it[0].geohash) }
        .distinctUntilChanged()

    val locationUpdates: Flow<LocationUpdate> = currentSelectionUpdates
        .flatMapLatest { selection ->
          when (selection) {
            is LocationSelection.Static -> flowOf(LocationUpdate.Loaded(selection, selection.location))
            LocationSelection.None -> flowOf(LocationUpdate.Error(selection))
            LocationSelection.FollowMe -> followMeUpdates
                .map<Location, LocationUpdate> { LocationUpdate.Loaded(selection, it) }
                .catch {
                  Timber.e(it, "Location updates failed.")
                  emit(LocationUpdate.Error(selection))
                }
                .onStart { emit(LocationUpdate.Loading(selection)) }
          }
        }

    val refreshEvents = refreshChannel.asFlow().onStart { emit(Unit) }

    val states: Flow<State> = combineTransform(locationUpdates, refreshEvents) { locationUpdate, _ ->
      when (locationUpdate) {
        is LocationUpdate.Loading -> {
          emit(State(locationUpdate.selection, loadingStatus = Loading))
        }

        is LocationUpdate.Error -> {
          emit(State(locationUpdate.selection, loadingStatus = LocationFailed))
        }

        is LocationUpdate.Loaded -> {
          emit(State(locationUpdate.selection, currentLocation = locationUpdate.location, loadingStatus = Loading))
          emit(loadForecasts(locationUpdate))
        }
      }
    }

    states
        .scanReduce { previousState: State, newState: State ->
          // If we're changing from one state to another and would lose our forecast information, check if we're
          // presenting the same location. If we are, we can reuse our previously calculated forecasts.
          if (newState.currentLocation == previousState.currentLocation && newState.forecasts == null) {
            newState.copy(forecasts = previousState.forecasts)
          } else {
            newState
          }
        }
        .onEach { statesChannel.offer(it) }
        .launchIn(scope)
  }

  fun enableLocationUpdates(enable: Boolean) {
    locationUpdatesToggleChannel.offer(enable)
  }

  fun observeStates(): Flow<State> = statesChannel.asFlow()

  fun destroy() {
    scope.cancel()
  }

  private suspend fun loadForecasts(locationUpdate: LocationUpdate.Loaded): State {
    try {
      val forecasts = getForecasts(locationUpdate.location)
      return State(
          currentSelection = locationUpdate.selection,
          currentLocation = locationUpdate.location,
          forecasts = forecasts,
          loadingStatus = Success
      )
    } catch (e: Exception) {
      Timber.e(e, "Failed to get forecasts for %s", locationUpdate.location.name)
      return State(
          currentSelection = locationUpdate.selection,
          currentLocation = locationUpdate.location,
          loadingStatus = NetworkFailed
      )
    }
  }

  private suspend fun getForecasts(location: Location): Forecasts = coroutineScope {
    // Request observations and date forecasts simultaneously.
    val observations = async { api.getObservations(location.geohash) }
    val dateForecasts = async { api.getDateForecasts(location.geohash) }
    return@coroutineScope Forecasts(
        observations.await(),
        dateForecasts.await()
    )
  }

  data class State(
      val currentSelection: LocationSelection,
      val currentLocation: Location? = null,
      val forecasts: Forecasts? = null,
      val loadingStatus: LoadingStatus = Loading
  )

  enum class LoadingStatus { Loading, LocationFailed, NetworkFailed, Success }

  private sealed class LocationUpdate(open val selection: LocationSelection) {
    data class Loading(override val selection: LocationSelection) : LocationUpdate(selection)
    data class Error(override val selection: LocationSelection) : LocationUpdate(selection)
    data class Loaded(override val selection: LocationSelection, val location: Location) : LocationUpdate(selection)
  }
}
