package codes.chrishorner.socketweather.rain_radar

import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import java.net.HttpURLConnection

/**
 * Only cache rain radar tiles for an hour.
 */
object RadarTileSourcePolicy : TileSourcePolicy(6, 0) {

  override fun computeExpirationTime(pHttpExpiresHeader: String?, pHttpCacheControlHeader: String?, now: Long): Long {
    return now + (60 * 60 * 1_000)
  }

  override fun computeExpirationTime(pHttpURLConnection: HttpURLConnection?, now: Long): Long {
    return now + (60 * 60 * 1_000)
  }
}
