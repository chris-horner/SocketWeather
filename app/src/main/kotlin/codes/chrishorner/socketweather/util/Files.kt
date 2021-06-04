package codes.chrishorner.socketweather.util

import android.os.StrictMode

/**
 * Explicitly perform disk operations that would normally violate [StrictMode].
 */
inline fun allowMainThreadDiskOperations(block: () -> Unit) {
  val diskReadPolicy = StrictMode.allowThreadDiskReads()
  val diskWritePolicy = StrictMode.allowThreadDiskWrites()
  block()
  StrictMode.setThreadPolicy(diskReadPolicy)
  StrictMode.setThreadPolicy(diskWritePolicy)
}
