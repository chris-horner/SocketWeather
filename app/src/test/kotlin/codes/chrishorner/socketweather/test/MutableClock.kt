package codes.chrishorner.socketweather.test

import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAmount

class MutableClock(startTime: OffsetDateTime) : Clock() {
  private var instant: Instant = startTime.toInstant()
  private var zone: ZoneId = startTime.toZonedDateTime().zone

  override fun withZone(zoneId: ZoneId): Clock {
    return MutableClock(instant.atZone(zoneId).toOffsetDateTime())
  }

  override fun getZone(): ZoneId = zone

  override fun instant(): Instant = instant

  fun advanceBy(amount: TemporalAmount) {
    instant = instant.plus(amount)
  }

  fun set(time: OffsetDateTime) {
    instant = time.toInstant()
    zone = time.offset
  }
}
