package codes.chrishorner.socketweather.debug

import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TextView
import au.com.gridstone.debugdrawer.DebugDrawerModule
import au.com.gridstone.debugdrawer.inflate
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.data.DebugDeviceLocator
import codes.chrishorner.socketweather.data.DebugDeviceLocator.Mode

class DebugDeviceLocatorModule(private val locator: DebugDeviceLocator) : DebugDrawerModule {

  override fun onCreateView(parent: ViewGroup): View {
    val view: View = parent.inflate(R.layout.debug_drawer_locator)
    val modeSpinner: Spinner = view.findViewById(R.id.debugDrawer_locationMode)
    val locationSpinner: Spinner = view.findViewById(R.id.debugDrawer_mockLocation)

    modeSpinner.adapter = ModeAdapter()
    modeSpinner.setSelection(Mode.values().indexOf(locator.currentMode))
    modeSpinner.onItemSelected { position ->
      val mode = Mode.values()[position]
      locator.setMode(mode)
      locationSpinner.isEnabled = mode == Mode.MOCK
    }

    locationSpinner.adapter = LocationAdapter(locator.locationNames)
    locationSpinner.setSelection(locator.locationNames.indexOf(locator.currentLocationName))
    locationSpinner.onItemSelected { position -> locator.setLocation(locator.locationNames[position]) }
    locationSpinner.isEnabled = locator.currentMode == Mode.MOCK

    return view
  }

  private class ModeAdapter : SpinnerAdapter<Mode>() {

    override fun getCount(): Int = Mode.values().size

    override fun getItem(position: Int): Mode = Mode.values()[position]

    override fun bindView(item: Mode, position: Int, view: TextView) {
      view.text = item.name
    }
  }

  private class LocationAdapter(private val names: List<String>) : SpinnerAdapter<String>() {

    override fun getCount(): Int = names.size

    override fun getItem(position: Int): String = names[position]

    override fun bindView(item: String, position: Int, view: TextView) {
      view.text = item
    }
  }
}
