package com.garitawatch.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.fcmDataStore by preferencesDataStore(name = "fcm_preferences")

@Singleton
class FcmRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fcmTokenKey = stringPreferencesKey("fcm_token")
    private val lastUpdatedKey = longPreferencesKey("fcm_token_last_updated")
    private val isSyncedKey = booleanPreferencesKey("fcm_token_synced")
    private val installationIdKey = stringPreferencesKey("installation_id")

    val fcmToken: Flow<String?> = context.fcmDataStore.data.map { preferences ->
        preferences[fcmTokenKey]
    }

    val lastUpdated: Flow<Long> = context.fcmDataStore.data.map { preferences ->
        preferences[lastUpdatedKey] ?: 0L
    }

    val isSynced: Flow<Boolean> = context.fcmDataStore.data.map { preferences ->
        preferences[isSyncedKey] ?: false
    }

    val installationId: Flow<String?> = context.fcmDataStore.data.map { preferences ->
        preferences[installationIdKey]
    }

    suspend fun getOrCreateInstallationId(): String {
        val currentId = installationId.first()
        if (currentId != null) return currentId

        val newId = UUID.randomUUID().toString()
        context.fcmDataStore.edit { preferences ->
            preferences[installationIdKey] = newId
        }
        return newId
    }

    suspend fun saveToken(token: String, timestamp: Long) {
        context.fcmDataStore.edit { preferences ->
            preferences[fcmTokenKey] = token
            preferences[lastUpdatedKey] = timestamp
            preferences[isSyncedKey] = false
        }
    }

    suspend fun markAsSynced() {
        context.fcmDataStore.edit { preferences ->
            preferences[isSyncedKey] = true
        }
    }

    suspend fun clearToken() {
        context.fcmDataStore.edit { preferences ->
            preferences.remove(fcmTokenKey)
            preferences.remove(lastUpdatedKey)
            preferences.remove(isSyncedKey)
        }
    }
}
