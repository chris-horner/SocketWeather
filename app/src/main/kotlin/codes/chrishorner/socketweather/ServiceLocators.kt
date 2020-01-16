package codes.chrishorner.socketweather

import android.app.Application
import android.content.Context
import androidx.annotation.MainThread
import codes.chrishorner.socketweather.data.DeviceLocator
import codes.chrishorner.socketweather.data.Forecaster
import codes.chrishorner.socketweather.data.LocationChoices
import codes.chrishorner.socketweather.data.NetworkComponents
import org.threeten.bp.Clock

// This file contains caches and functions to access singletons.
// Basically a bunch of cheap service locators.

private var deviceLocator: DeviceLocator? = null

@MainThread
fun Context.getDeviceLocator(): DeviceLocator {
  deviceLocator?.let { return it }
  return DeviceLocator(app).also { deviceLocator = it }
}

private var locationChoices: LocationChoices? = null

@MainThread
fun Context.getLocationChoices(): LocationChoices {
  locationChoices?.let { return it }
  return LocationChoices(app).also { locationChoices = it }
}

private var networkComponents: NetworkComponents? = null

@MainThread
fun Context.getNetworkComponents(): NetworkComponents {
  networkComponents?.let { return it }
  return NetworkComponents(app, getLocationChoices())
}

private var forecaster: Forecaster? = null

@MainThread
fun Context.getForecaster(): Forecaster {
  forecaster?.let { return it }

  return Forecaster(
      Clock.systemDefaultZone(),
      getNetworkComponents().api,
      getLocationChoices().observeCurrentSelection(),
      getDeviceLocator().observeDeviceLocation()
  ).also { forecaster = it }
}

private val Context.app: Application
  get() = applicationContext as Application
