package codes.chrishorner.socketweather.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import codes.chrishorner.socketweather.appSingletons
import codes.chrishorner.socketweather.util.Strings.AndroidStrings
import java.time.Duration
import java.util.concurrent.TimeUnit.MINUTES

class ForecastWidgetReceiver : GlanceAppWidgetReceiver() {

  override val glanceAppWidget = ForecastWidget()

  override fun onEnabled(context: Context) {
    // When widgets are placed, use WorkManager to try and update them every 2~ hours.
    val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(Duration.ofHours(2), Duration.ofHours(1))
      .setConstraints(
        Constraints.Builder()
          .setRequiredNetworkType(NetworkType.CONNECTED)
          .build()
      )
      .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, MINUTES)
      .addTag(WORK_TAG)
      .build()

    WorkManager.getInstance(context).enqueue(request)
  }

  override fun onDisabled(context: Context) {
    // When all widgets are deleted, cancel the periodic updates.
    WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
  }

  companion object {
    private const val WORK_TAG = "widget_update"
  }
}

class ForecastWidget : GlanceAppWidget() {

  companion object {
    // Define breakpoints for different width and height configurations.
    private val WIDTH_1U = 48.dp
    private val WIDTH_2U = 96.dp
    private val WIDTH_3U = 180.dp
    private val WIDTH_4U = 220.dp
    private val WIDTH_5U = 280.dp
    private val HEIGHT_1U = 48.dp
    private val HEIGHT_2U = 160.dp
    private val HEIGHT_3U = 260.dp
    private val HEIGHT_4U = 360.dp

    // Using the breakpoints, define widget configurations.
    private val TINY_BOX = DpSize(WIDTH_1U, HEIGHT_1U)
    private val TINY_ROW = DpSize(WIDTH_2U, HEIGHT_1U)
    private val SMALL_ROW = DpSize(WIDTH_3U, HEIGHT_1U)
    private val ROW = DpSize(WIDTH_4U, HEIGHT_1U)
    private val TINY_COLUMN = DpSize(WIDTH_1U, HEIGHT_2U)
    private val SMALL_COLUMN = DpSize(WIDTH_1U, HEIGHT_3U)
    private val COLUMN = DpSize(WIDTH_1U, HEIGHT_4U)
    private val WIDE_TINY_COLUMN = DpSize(WIDTH_2U, HEIGHT_2U)
    private val WIDE_SMALL_COLUMN = DpSize(WIDTH_2U, HEIGHT_3U)
    private val WIDE_COLUMN = DpSize(WIDTH_2U, HEIGHT_4U)
    private val SHORT_BOX = DpSize(WIDTH_2U, HEIGHT_2U)
    private val WIDE_SHORT_BOX = DpSize(WIDTH_3U, HEIGHT_2U)
    private val WIDER_SHORT_BOX = DpSize(WIDTH_5U, HEIGHT_2U)
    private val BOX = DpSize(WIDTH_4U, HEIGHT_3U)
    private val TALL_BOX = DpSize(WIDTH_4U, HEIGHT_4U)
    private val BIG_BOX = DpSize(WIDTH_5U, HEIGHT_3U)
    private val TALL_BIG_BOX = DpSize(WIDTH_5U, HEIGHT_4U)
  }

  override val sizeMode = SizeMode.Responsive(
    setOf(
      TINY_BOX,
      TINY_ROW,
      SMALL_ROW,
      ROW,
      TINY_COLUMN,
      SMALL_COLUMN,
      COLUMN,
      WIDE_TINY_COLUMN,
      WIDE_SMALL_COLUMN,
      WIDE_COLUMN,
      SHORT_BOX,
      WIDE_SHORT_BOX,
      WIDER_SHORT_BOX,
      BOX,
      TALL_BOX,
      BIG_BOX,
      TALL_BIG_BOX
    )
  )

  @SuppressLint("StateFlowValueCalledInComposition") // Get the current forecast once per invalidation.
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val forecast = context.appSingletons.stores.forecast.data.value ?: return
    val strings = AndroidStrings(context)
    val formattedForecast = forecast.formatForWidget(strings)

    when (LocalSize.current) {
      TINY_BOX -> Column(formattedForecast.dateForecasts, itemCount = 1)
      TINY_ROW -> TinyRow(formattedForecast.currentConditions)
      SMALL_ROW -> SmallRow(formattedForecast.currentConditions)
      ROW -> Row(formattedForecast.currentConditions)
      TINY_COLUMN -> Column(formattedForecast.dateForecasts, itemCount = 2)
      SMALL_COLUMN -> Column(formattedForecast.dateForecasts, itemCount = 3)
      COLUMN -> Column(formattedForecast.dateForecasts, itemCount = 4)
      WIDE_TINY_COLUMN -> Column(formattedForecast.dateForecasts, itemCount = 2, wide = true)
      WIDE_SMALL_COLUMN -> Column(formattedForecast.dateForecasts, itemCount = 3, wide = true)
      WIDE_COLUMN -> Column(formattedForecast.dateForecasts, itemCount = 4, wide = true)
      SHORT_BOX -> Column(formattedForecast.dateForecasts, itemCount = 2)
      WIDE_SHORT_BOX -> ShortBox(formattedForecast, hourlyCount = 4)
      WIDER_SHORT_BOX -> ShortBox(formattedForecast, hourlyCount = 5)
      BOX -> Box(formattedForecast, small = true, hourlyCount = 4, dayCount = 3)
      TALL_BOX -> Box(formattedForecast, small = true, hourlyCount = 4, dayCount = 6)
      BIG_BOX -> Box(formattedForecast, hourlyCount = 5, dayCount = 3)
      TALL_BIG_BOX -> Box(formattedForecast, hourlyCount = 5, dayCount = 6)
    }
  }
}
