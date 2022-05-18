package codes.chrishorner.socketweather.widget

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.data.ForecastError
import codes.chrishorner.socketweather.data.ForecastLoader.Result.Failure
import codes.chrishorner.socketweather.data.ForecastLoader.Result.Success
import timber.log.Timber

class WidgetUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    // Best effort to try and set the work as a foreground operation.
    try {
      setForeground(getForegroundInfo())
    } catch (e: IllegalArgumentException) {
      Timber.e(e, "Failed to run WidgetUpdateWorker in foreground.")
    }

    val forecastLoader = applicationContext.appSingletons.forecastLoader
    return when (val result = forecastLoader.synchronousRefresh()) {
      Success -> Result.success()
      is Failure -> {
        when (result.type) {
          ForecastError.NETWORK -> Result.retry()
          else -> Result.failure()
        }
      }
    }
  }

  override suspend fun getForegroundInfo(): ForegroundInfo {
    val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_notification_sync)
      .setContentTitle(applicationContext.getString(R.string.notification_title))
      .setOngoing(true)
      .build()

    val notificationManager = NotificationManagerCompat.from(applicationContext)
    val channel = NotificationChannelCompat.Builder(CHANNEL_ID, IMPORTANCE_LOW)
      .setName(applicationContext.getString(R.string.notification_name))
      .build()

    notificationManager.createNotificationChannel(channel)

    return if (Build.VERSION.SDK_INT >= 29) {
      ForegroundInfo(NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    } else {
      ForegroundInfo(NOTIFICATION_ID, notification)
    }
  }

  companion object {
    private const val NOTIFICATION_ID = 1
    private const val CHANNEL_ID = "widget_updater"
  }
}