package codes.chrishorner.socketweather

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import codes.chrishorner.socketweather.util.allowMainThreadDiskOperations
import com.jakewharton.threetenabp.AndroidThreeTen
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
      initialiseSingletons(this)
    }
  }
}
