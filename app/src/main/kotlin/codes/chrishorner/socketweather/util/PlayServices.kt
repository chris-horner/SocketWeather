package codes.chrishorner.socketweather.util

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun arePlayServicesAvailable(context: Context): Boolean {
  val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
  return result == ConnectionResult.SUCCESS
}

/**
 * Awaits for completion of the task without blocking a thread.
 *
 * This suspending function is cancellable.
 * If the Job of the current coroutine is cancelled or completed while this suspending function is waiting,
 * this function stops waiting for the completion stage and immediately resumes with [CancellationException].
 *
 * Stolen with ❤ from:
 * https://github.com/Kotlin/kotlinx.coroutines/tree/master/integration/kotlinx-coroutines-play-services
 */
suspend fun <T> Task<T>.await(): T {
  // fast path
  if (isComplete) {
    val e = exception
    return if (e == null) {
      if (isCanceled) {
        throw CancellationException("Task $this was cancelled normally.")
      } else {
        @Suppress("UNCHECKED_CAST")
        result as T
      }
    } else {
      throw e
    }
  }

  return suspendCancellableCoroutine { cont ->
    addOnCompleteListener {
      val e = exception
      if (e == null) {
        @Suppress("UNCHECKED_CAST")
        if (isCanceled) cont.cancel() else cont.resume(result as T)
      } else {
        cont.resumeWithException(e)
      }
    }
  }
}
