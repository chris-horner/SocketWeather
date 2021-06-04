package codes.chrishorner.socketweather

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@Suppress("unused") // It's used in AndroidManifest.xml.
class SocketWeatherApp : Application() {

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
      StrictMode.setThreadPolicy(ThreadPolicy.Builder().detectAll().penaltyDeath().build())
      StrictMode.setVmPolicy(
        VmPolicy.Builder()
          .detectActivityLeaks()
          .detectLeakedRegistrationObjects()
          .penaltyDeath()
          .build()
      )
    }

    // Explicitly initialise these dependencies on the main thread as they're
    // needed for the whole app to do its thing.
    allowMainThreadDiskOperations {
      AndroidThreeTen.init(this)
      initialiseSingletons()
    }

    if (BuildConfig.DEBUG) {
      // If our API environment ever changes, remove all saved location selections.
      val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

      scope.launch {
        appSingletons.networkComponents.environmentChanges.first()
        allowMainThreadDiskOperations {
          appSingletons.locationSelectionStore.clear()
        }
      }
    }
  }
}
