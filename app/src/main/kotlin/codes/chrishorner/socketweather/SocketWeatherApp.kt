package codes.chrishorner.socketweather

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import codes.chrishorner.socketweather.data.NetworkComponents
import codes.chrishorner.socketweather.data.initialisePersistenceFiles
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
              .detectLeakedClosableObjects()
              .detectLeakedRegistrationObjects()
              .penaltyDeath()
              .build()
      )
    }

    AndroidThreeTen.init(this)
    NetworkComponents.init(this)
    initialisePersistenceFiles(this)
  }
}
