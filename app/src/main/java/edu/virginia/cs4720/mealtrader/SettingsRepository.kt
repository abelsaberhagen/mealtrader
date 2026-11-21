package edu.virginia.cs4720.mealtrader

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class SortMode { RECENT, NAME }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val availableOnly: Boolean = false,
    val sortMode: SortMode = SortMode.RECENT
)

class SettingsRepository(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val availableOnlyKey = booleanPreferencesKey("available_only")
    private val sortKey = stringPreferencesKey("sort_mode")

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[themeKey]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            availableOnly = prefs[availableOnlyKey] ?: false,
            sortMode = prefs[sortKey]?.let { runCatching { SortMode.valueOf(it) }.getOrNull() }
                ?: SortMode.RECENT
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[themeKey] = mode.name }
    suspend fun setAvailableOnly(v: Boolean) = context.dataStore.edit { it[availableOnlyKey] = v }
    suspend fun setSortMode(mode: SortMode) = context.dataStore.edit { it[sortKey] = mode.name }
}