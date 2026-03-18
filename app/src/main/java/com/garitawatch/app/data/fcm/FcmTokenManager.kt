package com.garitawatch.app.data.fcm

import android.util.Log
import com.garitawatch.app.data.repository.FcmRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val fcmRepository: FcmRepository
) {
    companion object {
        private const val TAG = "FcmTokenManager"
        private val TOKEN_EXPIRATION_DAYS = TimeUnit.DAYS.toMillis(30)
    }

    suspend fun getToken(): String? {
        return fcmRepository.fcmToken.first()
    }

    suspend fun refreshTokenIfNeeded() {
        try {
            val currentToken = fcmRepository.fcmToken.first()
            val lastUpdated = fcmRepository.lastUpdated.first()
            val now = System.currentTimeMillis()

            if (currentToken == null || (now - lastUpdated) > TOKEN_EXPIRATION_DAYS) {
                Log.d(TAG, "Token is missing or expired. Fetching new token...")
                fetchAndSaveToken()
            } else {
                Log.d(TAG, "Stored token is still valid.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking/refreshing token", e)
        }
    }

    suspend fun fetchAndSaveToken() {
        try {
            val token = firebaseMessaging.token.await()
            updateStoredToken(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch FCM token", e)
        }
    }

    suspend fun updateStoredToken(newToken: String) {
        val oldToken = fcmRepository.fcmToken.first()
        if (newToken != oldToken) {
            Log.d(TAG, "New token received, updating storage.")
            fcmRepository.saveToken(newToken, System.currentTimeMillis())
            // Here you would trigger a sync to your backend if applicable
        } else {
            Log.d(TAG, "Token hasn't changed.")
        }
    }

    suspend fun clearToken() {
        fcmRepository.clearToken()
        try {
            firebaseMessaging.deleteToken().await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting FCM token", e)
        }
    }

    suspend fun markAsSynced() {
        fcmRepository.markAsSynced()
    }
}
