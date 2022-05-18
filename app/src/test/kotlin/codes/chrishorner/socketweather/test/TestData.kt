package codes.chrishorner.socketweather.test

import codes.chrishorner.socketweather.data.DeviceLocation
import codes.chrishorner.socketweather.data.Location
import java.time.ZoneId

object TestData {
  val deviceLocation1 = DeviceLocation(-37.798336, 144.978468)
  val deviceLocation2 = DeviceLocation(-37.829855, 144.886371)
  val location1 = Location(
    "1",
    "1",
    "Fakezroy",
    "VIC",
    deviceLocation1.latitude,
    deviceLocation1.longitude,
    ZoneId.of("Australia/Melbourne")
  )
  val location2 = Location(
    "2",
    "2",
    "Mockswood",
    "VIC",
    deviceLocation2.latitude,
    deviceLocation2.longitude,
    ZoneId.of("Australia/Melbourne")
  )
}