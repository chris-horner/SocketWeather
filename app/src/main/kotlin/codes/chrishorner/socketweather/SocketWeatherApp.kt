package codes.chrishorner.socketweather

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.work.Configuration
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations
import kotlinx.coroutines.runBlocking
import timber.log.Timber

@Suppress("unused") // It's used in AndroidManifest.xml.
class SocketWeatherApp : Application(), Configuration.Provider {

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
      initialiseSingletons()
    }

    // If our API environment ever changes, remove all saved location selections.
    appSingletons.networkComponents.addEnvironmentChangeAction {
      allowMainThreadDiskOperations {
        runBlocking {
          appSingletons.stores.clear()
        }
      }
    }
  }

  override fun getWorkManagerConfiguration(): Configuration {
    return Configuration.Builder()
      .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR)
      .build()
  }
}
