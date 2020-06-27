package codes.chrishorner.socketweather

import android.app.Application
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.NetworkComponents
import org.threeten.bp.Clock

/**
 * A cheap and dirty service locator that houses singletons used throughout
 * the app.
 */
object Singletons {

  private var deviceLocatorInstance: DeviceLocator? = null
  private var locationChoicesInstance: LocationChoices? = null
  private var networkComponentsInstance: NetworkComponents? = null
  private var forecasterInstance: Forecaster? = null

  val deviceLocator: DeviceLocator
    get() = checkNotNull(deviceLocatorInstance) { "Singletons.initialise() not called." }
  val locationChoices: LocationChoices
    get() = checkNotNull(locationChoicesInstance) { "Singletons.initialise() not called." }
  val networkComponents: NetworkComponents
    get() = checkNotNull(networkComponentsInstance) { "Singletons.initialise() not called." }
  val forecaster: Forecaster
    get() = checkNotNull(forecasterInstance) { "Singletons.initialise() not called." }

  @MainThread
  fun initialise(app: Application) {
    deviceLocatorInstance = BuildTypeConfig.getDeviceLocator(app)
    locationChoicesInstance = LocationChoices(app)
    networkComponentsInstance = NetworkComponents(app, locationChoices)
    forecasterInstance = Forecaster(
        clock = Clock.systemDefaultZone(),
        api = networkComponents.api,
        locationSelections = locationChoices.observeCurrentSelection(),
        deviceLocations = deviceLocator.observeDeviceLocation()
    )
  }
}
