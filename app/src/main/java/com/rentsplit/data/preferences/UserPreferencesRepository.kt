package com.rentsplit.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val RENT_DUE_DAY = intPreferencesKey("rent_due_day")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val APP_THEME = stringPreferencesKey("app_theme")
        val DEFAULT_SPLIT_TYPE = stringPreferencesKey("default_split_type")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val rentDueDay = preferences[PreferencesKeys.RENT_DUE_DAY] ?: 1
            val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: false
            val appTheme = preferences[PreferencesKeys.APP_THEME] ?: "SYSTEM"
            val defaultSplitType = preferences[PreferencesKeys.DEFAULT_SPLIT_TYPE] ?: "EQUAL"
            val hasCompletedOnboarding = preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
            UserPreferences(rentDueDay, notificationsEnabled, appTheme, defaultSplitType, hasCompletedOnboarding)
        }

    suspend fun updateRentDueDay(rentDueDay: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.RENT_DUE_DAY] = rentDueDay
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateAppTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme
        }
    }

    suspend fun updateDefaultSplitType(splitType: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_SPLIT_TYPE] = splitType
        }
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }
}

data class UserPreferences(
    val rentDueDay: Int,
    val notificationsEnabled: Boolean,
    val appTheme: String,
    val defaultSplitType: String,
    val hasCompletedOnboarding: Boolean
)
