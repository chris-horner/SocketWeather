package codes.chrishorner.socketweather.util

import android.os.StrictMode
import java.io.File

fun getOrCreateFile(directory: File, name: String): File {
  val file = File(directory, name)
  if (!file.exists()) file.createNewFile()
  return file
}

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
