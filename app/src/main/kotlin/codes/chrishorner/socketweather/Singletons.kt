package codes.chrishorner.socketweather

import android.app.Application
import android.content.Context
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.AndroidDeviceLocator
import codes.chrishorner.socketweather.data.AppDiskStores
import codes.chrishorner.socketweather.data.AppStores
import codes.chrishorner.socketweather.data.DataConfig
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.DeviceLocator2
import codes.chrishorner.socketweather.data.ForecastLoader
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.RealForecastLoader
import codes.chrishorner.socketweather.data.RealLocationResolver
import java.time.Clock

/**
 * A cheap and dirty service locator that houses singletons used throughout
 * the app.
 */
interface Singletons {
  val stores: AppStores
  val deviceLocator: DeviceLocator
  val deviceLocator2: DeviceLocator2
  val networkComponents: NetworkComponents
  val forecastLoader: ForecastLoader
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
  override val stores = AppDiskStores(app, DataConfig.moshi)
  override val deviceLocator: DeviceLocator = CurrentBuildTypeComponents.createDeviceLocator(app)
  override val deviceLocator2 = AndroidDeviceLocator(app)
  override val networkComponents = CurrentBuildTypeComponents.createNetworkComponents(app)
  override val forecastLoader = RealForecastLoader(
    clock = Clock.systemDefaultZone(),
    api = networkComponents.api,
    locationResolver = RealLocationResolver(deviceLocator2, networkComponents.api),
    forecastStore = stores.forecast,
    locationSelectionStore = stores.currentSelection,
  )
}
