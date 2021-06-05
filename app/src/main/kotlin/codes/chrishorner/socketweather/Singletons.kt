package codes.chrishorner.socketweather

import android.app.Application
import android.content.Context
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DataConfig
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.data.LocationSelectionDiskStore
import codes.chrishorner.socketweather.data.LocationSelectionStore
import codes.chrishorner.socketweather.data.NetworkComponents
import java.time.Clock

/**
 * A cheap and dirty service locator that houses singletons used throughout
 * the app.
 */
interface Singletons {
  val deviceLocator: DeviceLocator
  val locationSelectionStore: LocationSelectionStore
  val networkComponents: NetworkComponents
  val forecaster: Forecaster
}

private var singletons: Singletons? = null

// Although this technically doesn't need to be an extension on Context, it's
// useful to limit access to service locators to when only a Context is available.
@Suppress("unused")
val Context.appSingletons
  get() = checkNotNull(singletons) { "initialiseSingletons() must be called in Application." }

@MainThread
fun Application.initialiseSingletons() {
  singletons = SingletonCache(this)
}

private class SingletonCache(app: Application) : Singletons {
  override val deviceLocator: DeviceLocator = CurrentBuildTypeComponents.createDeviceLocator(app)
  override val locationSelectionStore = LocationSelectionDiskStore(app, DataConfig.moshi)
  override val networkComponents = CurrentBuildTypeComponents.createNetworkComponents(app)
  override val forecaster = Forecaster(
    clock = Clock.systemDefaultZone(),
    api = networkComponents.api,
    locationSelections = locationSelectionStore.currentSelection,
    deviceLocations = deviceLocator.observeDeviceLocation()
  )
}
