package codes.chrishorner.socketweather.debug

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.debugPreferences: DataStore<Preferences> get() = applicationContext.nonLeakingPreferences

// Remove once fixed: https://issuetracker.google.com/issues/184415662
private val Context.nonLeakingPreferences: DataStore<Preferences> by preferencesDataStore("debugSettings")

/**
 * Synchronously read the current [Preferences].
 */
fun DataStore<Preferences>.blockingGet() = runBlocking { data.first() }

/**
 * Synchronously read a value from the current [Preferences].
 */
fun <T> DataStore<Preferences>.blockingGet(key: Key<T>): T? {
  return blockingGet()[key]
}

/**
 * Given an Int preference, read it as the ordinal entry in an enum.
 */
inline fun <reified T : Enum<T>> Preferences.getEnum(key: Key<Int>): T? {
  val index = get(key) ?: return null
  return enumValues<T>()[index]
}

object DebugPreferenceKeys {
  val ENABLE_HEAP_DUMPS = booleanPreferencesKey("enableHeapDumps")
  val HTTP_LOG_LEVEL = intPreferencesKey("httpLogLevel")
  val ENDPOINT = intPreferencesKey("endpoint")
  val MOCK_HTTP_DELAY = longPreferencesKey("mockHttpDelay")
  val MOCK_HTTP_VARIANCE = intPreferencesKey("mockHttpVariance")
  val MOCK_HTTP_FAIL_RATE = intPreferencesKey("mockHttpFailRate")
  val MOCK_HTTP_ERROR_RATE = intPreferencesKey("mockHttpErrorRate")
  val MOCK_HTTP_ERROR_CODE = intPreferencesKey("mockHttpErrorCode")
}
