package codes.chrishorner.socketweather.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

fun Instant.localTimeAtZone(zoneId: ZoneId): LocalTime = LocalTime.from(this.atZone(zoneId))
