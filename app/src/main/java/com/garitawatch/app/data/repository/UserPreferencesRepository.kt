package com.garitawatch.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val locationAskedKey = booleanPreferencesKey("location_permission_asked")

    val hasAskedLocationPermission: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[locationAskedKey] ?: false
    }

    suspend fun setLocationPermissionAsked() {
        context.dataStore.edit { preferences ->
            preferences[locationAskedKey] = true
        }
    }
}
