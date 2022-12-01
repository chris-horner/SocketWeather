package codes.chrishorner.socketweather.widget

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.data.ForecastError
import codes.chrishorner.socketweather.data.ForecastLoader.Result.Failure
import codes.chrishorner.socketweather.data.ForecastLoader.Result.Success
import java.time.Duration
import java.util.concurrent.TimeUnit.MINUTES

class WidgetUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
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

    return ForegroundInfo(NOTIFICATION_ID, notification)
  }

  companion object {
    private const val NOTIFICATION_ID = 1
    private const val CHANNEL_ID = "widget_updater"
    private const val WORK_TAG = "widget_update"

    fun start(context: Context) {
      val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(Duration.ofHours(2), Duration.ofMinutes(30))
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, MINUTES)
        .addTag(WORK_TAG)
        .build()

      WorkManager.getInstance(context).enqueue(request)
      context.appSingletons.forecastLoader.refreshIfNecessary()
    }

    fun stop(context: Context) {
      WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
    }
  }
}
