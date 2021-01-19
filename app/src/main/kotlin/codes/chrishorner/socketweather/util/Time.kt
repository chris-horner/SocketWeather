package codes.chrishorner.socketweather.util

import org.threeten.bp.Instant
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId

fun Instant.localTimeAtZone(zoneId: ZoneId): LocalTime = LocalTime.from(this.atZone(zoneId))
