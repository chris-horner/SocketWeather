package codes.chrishorner.socketweather.data

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.datastore.core.DataStoreFactory
import codes.chrishorner.socketweather.util.getOrCreateFile
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

interface ForecastStore {
  val currentForecast: StateFlow<Forecast?>
  fun set(forecast: Forecast)
  suspend fun clear()
}

// TODO: Migrate to using this for reads instead of Forecaster.
class ForecastDiskStore(
  app: Application,
  moshi: Moshi,
) : ForecastStore {

  private val directory = app.getDir("forecast", MODE_PRIVATE)
  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  private val currentForecastStore = DataStoreFactory.create(
    MoshiSerializer<Forecast?>(moshi, default = null)
  ) {
    getOrCreateFile(directory, "current_forecast")
  }

  override val currentForecast: StateFlow<Forecast?>

  init {
    val storedForecast = runBlocking { currentForecastStore.data.first() }
    currentForecast = currentForecastStore.data.stateIn(scope, SharingStarted.Eagerly, storedForecast)
  }

  override fun set(forecast: Forecast) {
    scope.launch { currentForecastStore.updateData { forecast } }
  }

  override suspend fun clear() {
    scope.launch {
      currentForecastStore.updateData { null }
    }
  }
}
