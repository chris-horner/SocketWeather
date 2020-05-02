package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.LocationResolution.DeviceLocationError
import codes.chrishorner.socketweather.data.LocationResolution.NetworkError
import codes.chrishorner.socketweather.data.LocationResolution.NotInAustralia
import codes.chrishorner.socketweather.data.LocationResolution.Resolved
import codes.chrishorner.socketweather.data.LocationResolution.Searching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import timber.log.Timber

sealed class LocationResolution(open val selection: LocationSelection) {
  object Searching : LocationResolution(LocationSelection.FollowMe)
  object DeviceLocationError : LocationResolution(LocationSelection.FollowMe)
  object NetworkError : LocationResolution(LocationSelection.FollowMe)
  object NotInAustralia : LocationResolution(LocationSelection.FollowMe)
  data class Resolved(override val selection: LocationSelection, val location: Location) : LocationResolution(selection)
}

private val australiaLatitudeRange = -44.057002..-9.763686
private val australiaLongitudeRange = 112.169980..154.927992

/**
 * Produce a stream of [LocationResolution] objects that updates whenever a new
 * [LocationSelection] is made, or a new [DeviceLocation] is emitted (if the
 * current selection is `FollowMe`).
 */
fun resolveLocations(
    locationSelections: Flow<LocationSelection>,
    deviceLocations: Flow<DeviceLocation>,
    api: WeatherApi
): Flow<LocationResolution> {

  val followMeStates: Flow<LocationResolution> = deviceLocations
      .transformLatest { (latitude, longitude) ->
        if (latitude in australiaLatitudeRange && longitude in australiaLongitudeRange) {
          try {
            val searchResults = api.searchForLocation("$latitude,$longitude")
            val location = api.getLocation(searchResults[0].geohash)
            emit(Resolved(LocationSelection.FollowMe, location))
          } catch (e: Exception) {
            Timber.e(e, "Failed to resolve Location.")
            emit(NetworkError)
          }
        } else {
          emit(NotInAustralia)
        }
      }
      .catch {
        Timber.e(it, "Failed to get device location.")
        emit(DeviceLocationError)
      }
      .distinctUntilChanged()
      .onStart { emit(Searching) }

  return locationSelections.flatMapLatest { selection ->
    when (selection) {
      is LocationSelection.Static -> flowOf(Resolved(selection, selection.location))
      LocationSelection.None -> flowOf(DeviceLocationError)
      LocationSelection.FollowMe -> followMeStates
    }
  }
}
