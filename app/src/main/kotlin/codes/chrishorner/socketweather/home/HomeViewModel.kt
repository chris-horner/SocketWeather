package codes.chrishorner.socketweather.home

import codes.chrishorner.socketweather.data.CurrentObservations
import codes.chrishorner.socketweather.data.DateForecast
import codes.chrishorner.socketweather.data.DeviceLocation
import codes.chrishorner.socketweather.data.Location
import codes.chrishorner.socketweather.data.LocationSelection
import codes.chrishorner.socketweather.data.WeatherApi
import codes.chrishorner.socketweather.home.HomeViewModel.LoadingStatus.Loading
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart

class HomeViewModel(
    private val api: WeatherApi,
    private val savedSelectionUpdates: Flow<Set<LocationSelection>>,
    private val currentSelectionUpdates: Flow<LocationSelection>,
    private val deviceLocationUpdates: Flow<DeviceLocation>
) {

  private val scope = MainScope()
  private val statesChannel = ConflatedBroadcastChannel<State>()

  init {
    // NOTE: Don't forget to multicast!

    val followMeUpdates = deviceLocationUpdates
        .mapLatest { api.searchForLocation(it.latitude, it.longitude) }
        .map { api.getLocation(it[0].geohash) }
        .distinctUntilChanged()

    val locationUpdates: Flow<LocationUpdate> = currentSelectionUpdates
        .flatMapLatest {
          when (it) {
            is LocationSelection.Static -> flowOf(it.location)
            LocationSelection.None -> emptyFlow()
            LocationSelection.FollowMe -> followMeUpdates
          }
        }
        .map<Location, LocationUpdate> { LocationUpdate.Loaded(it) }
        .catch { emit(LocationUpdate.Error) }
        .onStart { emit(LocationUpdate.Loading) }

    combine(currentSelectionUpdates, savedSelectionUpdates) { selection, set -> selection to set }
  }

  fun destroy() {
    scope.cancel()
  }

  data class State(
      val currentSelection: LocationSelection,
      val currentLocation: Location? = null,
      val savedSelections: Set<LocationSelection> = emptySet(),
      val observations: CurrentObservations? = null,
      val dateForecasts: List<DateForecast> = emptyList(),
      val loadingStatus: LoadingStatus = Loading
  )

  enum class LoadingStatus { Loading, LocationFailed, NetworkFailed }

  private sealed class LocationUpdate {
    object Loading : LocationUpdate()
    object Error : LocationUpdate()
    data class Loaded(val location: Location) : LocationUpdate()
  }
}
