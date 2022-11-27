package codes.chrishorner.socketweather.rain_radar.map

import android.content.Context
import android.graphics.Color
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.tileprovider.MapTileProviderBasic
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.util.MapTileIndex.getX
import org.osmdroid.util.MapTileIndex.getY
import org.osmdroid.util.MapTileIndex.getZoom
import org.osmdroid.views.overlay.TilesOverlay
import java.net.HttpURLConnection
import java.time.Duration

fun getTileProvider(context: Context): MapTileProviderBase {
  val source = object : OnlineTileSourceBase(
    "Terrain",
    0,
    18,
    256,
    "@2x.png",
    arrayOf(
      "https://stamen-tiles-a.a.ssl.fastly.net/terrain",
      "https://stamen-tiles-b.a.ssl.fastly.net/terrain",
      "https://stamen-tiles-c.a.ssl.fastly.net/terrain",
      "https://stamen-tiles-d.a.ssl.fastly.net/terrain",
    ),
    "© OpenStreetMap contributors",
    TileSourcePolicy(4, 0)
  ) {
    override fun getTileURLString(pMapTileIndex: Long): String {
      return "$baseUrl/${getZoom(pMapTileIndex)}/${getX(pMapTileIndex)}/${ getY(pMapTileIndex)}$mImageFilenameEnding"
    }
  }

  return MapTileProviderBasic(context, source)
}

fun getRainRadarOverlays(context: Context, timestamps: List<String>): List<TilesOverlay> {
  return timestamps.map { getRainOverlay(context, it) }
}

val TilesOverlay.isLoading: Boolean
  get() = !tileStates.isDone || tileStates.notFound > 0

private fun getRainOverlay(context: Context, timestamp: String): TilesOverlay {
  val source = object : OnlineTileSourceBase(
    timestamp,
    3,
    10,
    256,
    ".png",
    arrayOf("https://radar-tiles.service.bom.gov.au/tiles/$timestamp"),
    "© Australian Bureau of Meteorology",
    RadarTileSourcePolicy
  ) {
    override fun getTileURLString(pMapTileIndex: Long): String {
      return "$baseUrl/${getZoom(pMapTileIndex)}/${getX(pMapTileIndex)}/${
        getY(
          pMapTileIndex
        )
      }$mImageFilenameEnding"
    }
  }
  val provider = MapTileProviderBasic(context, source)
  val overlay = TilesOverlay(provider, context)
  overlay.loadingBackgroundColor = Color.TRANSPARENT
  return overlay
}

/**
 * Only cache rain radar tiles for an hour.
 */
private object RadarTileSourcePolicy : TileSourcePolicy(6, 0) {

  override fun computeExpirationTime(pHttpExpiresHeader: String?, pHttpCacheControlHeader: String?, now: Long): Long {
    return now + Duration.ofHours(1).toMillis()
  }

  override fun computeExpirationTime(pHttpURLConnection: HttpURLConnection?, now: Long): Long {
    return now + Duration.ofHours(1).toMillis()
  }
}
