package codes.chrishorner.socketweather.data

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val COUNT = 6
private val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

/**
 * Generate a list of timestamps for the past hour. These can be used with BOM's rain
 * radar map tile API to fetch rainfall images at a particular time. Timestamps are
 * ordered from oldest to newest.
 *
 * These timestamps start at the nearest 10 minute interval from the current time,
 * minus an additional 10 minutes.
 *
 * If the current time is `12:12`, the most resent timestamp will be for `12:00`
 */
fun generateRainRadarTimestamps(clock: Clock = Clock.systemDefaultZone()): List<String> {
  val nowUtc = LocalDateTime.now(clock.withZone(ZoneOffset.UTC))
  val minutesAtNearestTen = (nowUtc.minute / 10) * 10
  val nowAtNearestTenMinutes = nowUtc.withMinute(minutesAtNearestTen)

  val list = ArrayList<String>(COUNT)

  for (i in 0 until COUNT) {
    val intervalMinutes = (i + 1) * 10
    val time = nowAtNearestTenMinutes.minusMinutes(intervalMinutes.toLong())
    list.add(0, time.format(formatter))
  }

  return list
}
