package codes.chrishorner.socketweather.debug

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.debugPreferences: DataStore<Preferences> get() = applicationContext.nonLeakingPreferences

// Remove once fixed: https://issuetracker.google.com/issues/184415662
private val Context.nonLeakingPreferences: DataStore<Preferences> by preferencesDataStore("debugSettings")

object DebugPreferenceKeys {
  val ENABLE_HEAP_DUMPS = booleanPreferencesKey("enableHeapDumps")
}
