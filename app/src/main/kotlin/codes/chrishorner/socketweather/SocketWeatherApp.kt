package codes.chrishorner.socketweather

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber

@Suppress("unused") // It's used in AndroidManifest.xml.
class SocketWeatherApp : Application() {

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
      StrictMode.setThreadPolicy(ThreadPolicy.Builder().detectAll().penaltyDeath().build())
      StrictMode.setVmPolicy(VmPolicy.Builder().detectAll().penaltyDeath().build())
    }

    AndroidThreeTen.init(this)
  }
}
