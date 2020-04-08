package codes.chrishorner.socketweather

import android.content.Context
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.RealDeviceLocator
import codes.chrishorner.socketweather.util.app

private var deviceLocator: DeviceLocator? = null

@MainThread
fun Context.getDeviceLocator(): DeviceLocator {
  deviceLocator?.let { return it }
  return RealDeviceLocator(app).also { deviceLocator = it }
}
