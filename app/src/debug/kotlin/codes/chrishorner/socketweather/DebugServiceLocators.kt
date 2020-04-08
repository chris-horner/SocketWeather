package codes.chrishorner.socketweather

import android.content.Context
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DebugDeviceLocator
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.util.app

private var deviceLocator: DeviceLocator? = null

@MainThread
fun Context.getDeviceLocator(): DeviceLocator {
  deviceLocator?.let { return it }
  return DebugDeviceLocator(app).also { deviceLocator = it }
}

@MainThread
fun Context.getDebugDeviceLocator(): DebugDeviceLocator {
  return getDeviceLocator() as DebugDeviceLocator
}
