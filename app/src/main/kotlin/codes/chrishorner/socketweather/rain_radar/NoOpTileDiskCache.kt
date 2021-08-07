package codes.chrishorner.socketweather.rain_radar

import android.graphics.drawable.Drawable
import org.osmdroid.tileprovider.modules.IFilesystemCache
import org.osmdroid.tileprovider.tilesource.ITileSource
import java.io.InputStream

/**
 * Avoids writing anything to disk. Useful to rain radar layers where they
 * change constantly and we want to avoid caching them.
 */
object NoOpTileDiskCache : IFilesystemCache {

  override fun saveFile(
    pTileSourceInfo: ITileSource?,
    pMapTileIndex: Long,
    pStream: InputStream?,
    pExpirationTime: Long?
  ) = false

  override fun exists(pTileSourceInfo: ITileSource?, pMapTileIndex: Long) = false
  override fun onDetach() = Unit
  override fun remove(tileSource: ITileSource?, pMapTileIndex: Long) = true
  override fun getExpirationTimestamp(pTileSource: ITileSource?, pMapTileIndex: Long) = null
  override fun loadTile(pTileSource: ITileSource, pMapTileIndex: Long): Drawable? {
    return null
  }
}
