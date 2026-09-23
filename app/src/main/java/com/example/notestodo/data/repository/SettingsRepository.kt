package com.example.notestodo.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { System, Light, Dark }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
)

// One small preferences file, created the first time something is written to it.
private val Context.settingsDataStore by preferencesDataStore(name = "settings")

// DataStore is the modern replacement for SharedPreferences: reads arrive as a Flow, and
// writes are suspending, so neither blocks the main thread.
class SettingsRepository(private val context: Context) {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val dynamicColorKey = booleanPreferencesKey("dynamic_color")

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            // Falls back to the default when nothing is stored yet, or the stored name
            // is one this version no longer knows.
            themeMode = ThemeMode.entries.firstOrNull { it.name == preferences[themeModeKey] }
                ?: ThemeMode.System,
            dynamicColor = preferences[dynamicColorKey] ?: true,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[themeModeKey] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { it[dynamicColorKey] = enabled }
    }
}
