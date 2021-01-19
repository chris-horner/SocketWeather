package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.Rain.Amount
import com.squareup.moshi.JsonDataException
import org.threeten.bp.Clock
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.Period
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import java.io.IOException

class TestApi(clock: Clock) : WeatherApi {

  enum class ResponseMode { SUCCESS, NETWORK_ERROR, DATA_ERROR }

  private val firstDayInstant = LocalDate.now(clock).atTime(0, 0).toInstant(ZoneOffset.UTC)
  private val startingInstant = LocalDateTime.now(clock).toInstant(ZoneOffset.UTC)

  var responseMode: ResponseMode = ResponseMode.SUCCESS
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
  private val locations = listOf(location1, location2)

  override suspend fun searchForLocation(query: String): List<SearchResult> {
    failIfNecessary()

    return when {
      query.contains(',') -> {
        val (lat, lng) = query.split(',').map { it.toDouble() }
        listOf(locations.single { it.latitude == lat && it.longitude == lng }.toSearchResult())
      }
      else -> {
        locations.map { it.toSearchResult() }.filter { it.name.contains(query) }
      }
    }
  }

  override suspend fun getLocation(geohash: String): Location {
    failIfNecessary()

    return when (geohash) {
      "1" -> location1
      "2" -> location2
      else -> throw IllegalArgumentException()
    }
  }

  override suspend fun getObservations(geohash: String): CurrentObservations {
    failIfNecessary()
    return CurrentObservations(
        temp = 21f,
        temp_feels_like = 20f,
        wind = Wind(speed_kilometre = 0, direction = "N"),
        humidity = 98,
        station = Station("PlayStation")
    )
  }

  override suspend fun getDateForecasts(geohash: String): List<DateForecast> {
    failIfNecessary()

    return listOf(
        DateForecast(
            date = firstDayInstant,
            temp_min = null,
            temp_max = 20,
            extended_text = "Partly cloudy. Afternoon showers.",
            short_text = "Showers.",
            icon_descriptor = "showers",
            rain = Rain(Amount(4f, 8f, "mm"), chance = 80),
            uv = Uv(
                max_index = 13,
                start_time = LocalDate.now().atTime(6, 10).toInstant(ZoneOffset.UTC),
                end_time = LocalDate.now().plusDays(1).atTime(21, 40).toInstant(ZoneOffset.UTC)
            ),
            now = CurrentInformation(
                is_night = false,
                now_label = "Overnight Min",
                later_label = "Tomorrow's Max",
                temp_now = 14,
                temp_later = 27
            )
        ),
        DateForecast(
            date = firstDayInstant.plus(Period.ofDays(1)),
            temp_min = 14,
            temp_max = 27,
            extended_text = "Mostly sunny. The chance of fog about the outer southeast suburbs in the early morning. Areas of haze. Winds southerly 15 to 20 km/h increasing to 25 km/h before turning east to southeasterly 15 to 20 km/h during the day.",
            short_text = "Hazy.",
            icon_descriptor = "hazy",
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            uv = Uv(
                max_index = 14,
                start_time = LocalDate.now().plusDays(1).atTime(6, 10).toInstant(ZoneOffset.UTC),
                end_time = LocalDate.now().plusDays(2).atTime(21, 30).toInstant(ZoneOffset.UTC)
            )
        ),
        DateForecast(
            date = firstDayInstant.plus(Period.ofDays(2)),
            temp_min = 15,
            temp_max = 28,
            extended_text = "Mostly sunny. The chance of fog in the early morning, mainly over the western suburbs. Areas of haze in the morning and afternoon. Light winds becoming southerly 20 to 30 km/h in the early afternoon.",
            short_text = "Hazy at times.",
            icon_descriptor = "hazy",
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            uv = Uv(
                max_index = 14,
                start_time = LocalDate.now().plusDays(2).atTime(6, 10).toInstant(ZoneOffset.UTC),
                end_time = LocalDate.now().plusDays(3).atTime(21, 30).toInstant(ZoneOffset.UTC)
            )
        ),
        DateForecast(
            date = firstDayInstant.plus(Period.ofDays(3)),
            temp_min = 15,
            temp_max = 23,
            extended_text = "Partly cloudy. Winds southerly 15 to 25 km/h.",
            short_text = "Partly cloudy.",
            icon_descriptor = "mostly_sunny",
            rain = Rain(Amount(0f, null, "mm"), chance = 5),
            uv = Uv(
                max_index = 14,
                start_time = LocalDate.now().plusDays(3).atTime(6, 10).toInstant(ZoneOffset.UTC),
                end_time = LocalDate.now().plusDays(4).atTime(21, 30).toInstant(ZoneOffset.UTC)
            )
        ),
        DateForecast(
            date = firstDayInstant.plus(Period.ofDays(4)),
            temp_min = 13,
            temp_max = 26,
            extended_text = "Mostly sunny. Winds southerly 15 to 20 km/h becoming light during the evening.",
            short_text = "Mostly sunny.",
            icon_descriptor = "mostly_sunny",
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            uv = Uv(
                max_index = null,
                start_time = null,
                end_time = null
            )
        ),
        DateForecast(
            date = firstDayInstant.plus(Period.ofDays(5)),
            temp_min = 14,
            temp_max = 37,
            extended_text = "Hot and mostly sunny. Light winds becoming north to northwesterly 15 to 20 km/h during the morning.",
            short_text = "Mostly sunny.",
            icon_descriptor = "mostly_sunny",
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            uv = Uv(
                max_index = null,
                start_time = null,
                end_time = null
            )
        ),
        DateForecast(
            date = firstDayInstant.plus(Period.ofDays(6)),
            temp_min = 24,
            temp_max = 40,
            extended_text = "Very hot. Partly cloudy. The chance of a thunderstorm with little or no rainfall during the afternoon and evening. Winds northerly 20 to 30 km/h turning northwesterly 25 to 35 km/h during the morning.",
            short_text = "Hot. Partly cloudy.",
            icon_descriptor = "mostly_sunny",
            rain = Rain(Amount(0f, null, "mm"), chance = 10),
            uv = Uv(
                max_index = null,
                start_time = null,
                end_time = null
            )
        ),
        DateForecast(
            date = firstDayInstant.plus(Period.ofDays(7)),
            temp_min = 25,
            temp_max = 41,
            extended_text = "Hot. Partly cloudy. Medium (60%) chance of showers, most likely later in the day. The chance of a thunderstorm. Winds northerly 25 to 40 km/h shifting cooler southwesterly 20 to 30 km/h later in the day.",
            short_text = "Hot. Cool change later.",
            icon_descriptor = "shower",
            rain = Rain(Amount(0f, 8f, "mm"), chance = 60),
            uv = Uv(
                max_index = null,
                start_time = null,
                end_time = null
            )
        )
    )
  }

  override suspend fun getThreeHourlyForecasts(geohash: String): List<ThreeHourlyForecast> {
    failIfNecessary()

    return listOf(
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 17,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "mostly_sunny",
            time = startingInstant,
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 15,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "mostly_sunny",
            time = startingInstant.plus(Duration.ofHours(3)),
            is_night = true
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 15,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "mostly_sunny",
            time = startingInstant.plus(Duration.ofHours(6)),
            is_night = true
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 14,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "mostly_sunny",
            time = startingInstant.plus(Duration.ofHours(6)),
            is_night = true
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 15,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(12)),
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 22,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(15)),
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 27,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(18)),
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 28,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(21)),
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 25,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(24)),
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 20,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(27)),
            is_night = true
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 17,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(30)),
            is_night = true
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 15,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(33)),
            is_night = true
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 17,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(36)),
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 24,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "hazy",
            time = startingInstant.plus(Duration.ofHours(39)),
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 28,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "mostly_sunny",
            time = startingInstant.plus(Duration.ofHours(41)),
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 28,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "sunny",
            time = startingInstant.plus(Duration.ofHours(44)),
            is_night = false
        ),
        ThreeHourlyForecast(
            rain = Rain(Amount(0f, null, "mm"), chance = 0),
            temp = 22,
            wind = Wind(speed_kilometre = 0, direction = "N"),
            icon_descriptor = "sunny",
            time = startingInstant.plus(Duration.ofHours(47)),
            is_night = false
        )
    )
  }

  private fun Location.toSearchResult() = SearchResult(id, geohash, name, "TEST", state)

  private fun failIfNecessary() {
    if (responseMode == ResponseMode.NETWORK_ERROR) throw IOException("TestApi failure.")
    else if (responseMode == ResponseMode.DATA_ERROR) throw JsonDataException("TestApi failure.")
  }
}
