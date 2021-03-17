package codes.chrishorner.socketweather.common

import androidx.annotation.DrawableRes
import codes.chrishorner.socketweather.R

@DrawableRes
fun weatherIconRes(descriptor: String, night: Boolean = false): Int = when (descriptor) {
  "sunny" -> R.drawable.ic_weather_sunny_24dp
  "clear" -> if (night) R.drawable.ic_weather_clear_night_24dp else R.drawable.ic_weather_sunny_24dp
  "mostly_sunny", "partly_cloudy" -> if (night) R.drawable.ic_weather_partly_cloudy_night_24dp else R.drawable.ic_weather_partly_cloudy_24dp
  "cloudy" -> R.drawable.ic_weather_cloudy_24dp
  "hazy" -> if (night) R.drawable.ic_weather_hazy_night_24dp else R.drawable.ic_weather_hazy_24dp
  "light_rain", "light_shower" -> R.drawable.ic_weather_light_rain_24dp
  "windy" -> R.drawable.ic_weather_windy_24dp
  "fog" -> R.drawable.ic_weather_fog_24dp
  "shower", "rain", "heavy_shower" -> R.drawable.ic_weather_rain_24dp
  "dusty" -> R.drawable.ic_weather_dusty_24dp
  "frost" -> R.drawable.ic_weather_frost_24dp
  "snow" -> R.drawable.ic_weather_snow_24dp
  "storm" -> R.drawable.ic_weather_storm_24dp
  "cyclone" -> R.drawable.ic_weather_cyclone_24dp
  else -> R.drawable.ic_weather_unknown_24dp
}
