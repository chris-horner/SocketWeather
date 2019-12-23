package codes.chrishorner.socketweather.data

import codes.chrishorner.socketweather.data.Rain.Amount
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.Period
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.MockRetrofit
import retrofit2.mock.create

class MockWeatherApi(mockRetrofit: MockRetrofit) : WeatherApi {

  private val delegate: BehaviorDelegate<WeatherApi> = mockRetrofit.create()

  override suspend fun searchForLocation(query: String): List<SearchResult> {
    return delegate.returningResponse(listOf(searchResult)).searchForLocation(query)
  }

  override suspend fun searchForLocation(latitude: Double, longitude: Double): List<SearchResult> {
    return delegate.returningResponse(searchResult).searchForLocation(latitude, longitude)
  }

  override suspend fun getLocation(geohash: String): Location {
    return delegate.returningResponse(location).getLocation(geohash)
  }

  override suspend fun getObservations(geohash: String): CurrentObservations {
    return delegate.returningResponse(currentObservations).getObservations(geohash)
  }

  override suspend fun getDateForecasts(geohash: String): List<DateForecast> {
    return delegate.returningResponse(generateDateForecasts()).getDateForecasts(geohash)
  }

  override suspend fun getThreeHourlyForecasts(geohash: String): List<ThreeHourlyForecast> {
    return delegate.returningResponse(generateThreeHourlyForecasts()).getThreeHourlyForecasts(geohash)
  }

  private companion object MockData {

    val searchResult = SearchResult(
        id = "Fakezroy-r1r0gnd",
        geohash = "r1r0gnd",
        name = "Fakezroy",
        postcode = "3065",
        state = "VIC"
    )

    val location = Location(
        id = "Fakezroy-r1r0gnd",
        geohash = "r1r0gnd",
        name = "Fakezroy",
        state = "VIC",
        latitude = -37.80052185058594,
        longitude = 144.97901916503906,
        timezone = ZoneId.of("Australia/Melbourne")
    )

    val currentObservations = CurrentObservations(
        temp = 17f,
        temp_feels_like = 15.8f,
        station = Station(name = "Melbourne (Olympic Park)", distance = 2849)
    )

    fun generateDateForecasts(): List<DateForecast> {
      val firstDayInstant = LocalDate.now().atTime(0, 0).toInstant(ZoneOffset.UTC)
      val isNight = LocalTime.now().isAfter(LocalTime.of(20, 0)) && LocalTime.now().isBefore(LocalTime.of(6, 0))
      return listOf(
          DateForecast(
              date = firstDayInstant,
              temp_min = null,
              temp_max = 22,
              extended_text = "Partly cloudy. Areas of haze. Winds southerly 20 to 30 km/h decreasing to 15 to 20 km/h in the evening.",
              short_text = "Hazy.",
              icon_descriptor = "hazy",
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              now = CurrentInformation(
                  is_night = isNight,
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
              rain = Rain(Amount(0, null, "mm"), chance = 0)
          ),
          DateForecast(
              date = firstDayInstant.plus(Period.ofDays(2)),
              temp_min = 15,
              temp_max = 28,
              extended_text = "Mostly sunny. The chance of fog in the early morning, mainly over the western suburbs. Areas of haze in the morning and afternoon. Light winds becoming southerly 20 to 30 km/h in the early afternoon.",
              short_text = "Hazy at times.",
              icon_descriptor = "hazy",
              rain = Rain(Amount(0, null, "mm"), chance = 0)
          ),
          DateForecast(
              date = firstDayInstant.plus(Period.ofDays(3)),
              temp_min = 15,
              temp_max = 23,
              extended_text = "Partly cloudy. Winds southerly 15 to 25 km/h.",
              short_text = "Partly cloudy.",
              icon_descriptor = "mostly_sunny",
              rain = Rain(Amount(0, null, "mm"), chance = 5)
          ),
          DateForecast(
              date = firstDayInstant.plus(Period.ofDays(4)),
              temp_min = 13,
              temp_max = 26,
              extended_text = "Mostly sunny. Winds southerly 15 to 20 km/h becoming light during the evening.",
              short_text = "Mostly sunny.",
              icon_descriptor = "mostly_sunny",
              rain = Rain(Amount(0, null, "mm"), chance = 0)
          ),
          DateForecast(
              date = firstDayInstant.plus(Period.ofDays(5)),
              temp_min = 14,
              temp_max = 37,
              extended_text = "Hot and mostly sunny. Light winds becoming north to northwesterly 15 to 20 km/h during the morning.",
              short_text = "Mostly sunny.",
              icon_descriptor = "mostly_sunny",
              rain = Rain(Amount(0, null, "mm"), chance = 0)
          ),
          DateForecast(
              date = firstDayInstant.plus(Period.ofDays(6)),
              temp_min = 24,
              temp_max = 40,
              extended_text = "Very hot. Partly cloudy. The chance of a thunderstorm with little or no rainfall during the afternoon and evening. Winds northerly 20 to 30 km/h turning northwesterly 25 to 35 km/h during the morning.",
              short_text = "Hot. Partly cloudy.",
              icon_descriptor = "mostly_sunny",
              rain = Rain(Amount(0, null, "mm"), chance = 10)
          ),
          DateForecast(
              date = firstDayInstant.plus(Period.ofDays(7)),
              temp_min = 25,
              temp_max = 41,
              extended_text = "Hot. Partly cloudy. Medium (60%) chance of showers, most likely later in the day. The chance of a thunderstorm. Winds northerly 25 to 40 km/h shifting cooler southwesterly 20 to 30 km/h later in the day.",
              short_text = "Hot. Cool change later.",
              icon_descriptor = "shower",
              rain = Rain(Amount(0, 8, "mm"), chance = 60)
          )
      )
    }

    fun generateThreeHourlyForecasts(): List<ThreeHourlyForecast> {
      val startingInstant = LocalDate.now().atTime(0, 9).toInstant(ZoneOffset.UTC)
      return listOf(
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 17,
              icon_descriptor = "mostly_sunny",
              time = startingInstant,
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 15,
              icon_descriptor = "mostly_sunny",
              time = startingInstant.plus(Duration.ofHours(3)),
              is_night = true
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 15,
              icon_descriptor = "mostly_sunny",
              time = startingInstant.plus(Duration.ofHours(6)),
              is_night = true
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 14,
              icon_descriptor = "mostly_sunny",
              time = startingInstant.plus(Duration.ofHours(6)),
              is_night = true
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 15,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(12)),
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 22,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(15)),
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 27,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(18)),
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 28,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(21)),
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 25,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(24)),
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 20,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(27)),
              is_night = true
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 17,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(30)),
              is_night = true
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 15,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(33)),
              is_night = true
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 17,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(36)),
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 24,
              icon_descriptor = "hazy",
              time = startingInstant.plus(Duration.ofHours(39)),
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 28,
              icon_descriptor = "mostly_sunny",
              time = startingInstant.plus(Duration.ofHours(41)),
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 28,
              icon_descriptor = "sunny",
              time = startingInstant.plus(Duration.ofHours(44)),
              is_night = false
          ),
          ThreeHourlyForecast(
              rain = Rain(Amount(0, null, "mm"), chance = 0),
              temp = 22,
              icon_descriptor = "sunny",
              time = startingInstant.plus(Duration.ofHours(47)),
              is_night = false
          )
      )
    }
  }
}
