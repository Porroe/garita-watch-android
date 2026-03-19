package com.garitawatch.app.data.remote

import android.content.Context
import android.os.Build
import android.util.Log
import com.garitawatch.app.data.repository.FcmRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class DeviceSubscription(
    val installation_id: String,
    val fcm_token: String,
    val platform: String = "android",
    val locale: String,
    val user_agent: String,
    val notification_permission: String,
    val last_seen_at: String,
    val is_active: Boolean = true
)

@Serializable
data class WaitTimeAlert(
    val installation_id: String,
    val port_number: String,
    val port_name: String,
    val crossing_name: String?,
    val travel_mode: String,
    val lane_type: String,
    val operator: String = "lte",
    val threshold_minutes: Int,
    val is_active: Boolean = true,
    val is_triggered: Boolean = false,
    val created_at: String,
    val updated_at: String
)

@Singleton
class SupabaseManager @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val fcmRepository: FcmRepository,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SupabaseManager"
        private const val TABLE_DEVICE_SUBSCRIPTIONS = "device_subscriptions"
        private const val TABLE_WAIT_TIME_ALERTS = "wait_time_alerts"
        
        private val VALID_TRAVEL_MODES = setOf("passenger", "pedestrian", "commercial")
        private val VALID_LANE_TYPES = setOf("standard", "ready", "nexus_sentri", "fast")
    }

    suspend fun registerDeviceToken(fcmToken: String) {
        if (fcmToken.isBlank()) {
            Log.w(TAG, "Attempted to register empty FCM token")
            return
        }

        try {
            val installationId = fcmRepository.getOrCreateInstallationId()
            
            val nowIso = getCurrentIsoTimestamp()

            val subscription = DeviceSubscription(
                installation_id = installationId,
                fcm_token = fcmToken,
                platform = "android",
                locale = Locale.getDefault().toString(),
                user_agent = "Android ${Build.VERSION.RELEASE}; ${Build.MODEL}",
                notification_permission = if (checkNotificationPermission()) "granted" else "denied",
                last_seen_at = nowIso,
                is_active = true
            )

            supabaseClient.postgrest[TABLE_DEVICE_SUBSCRIPTIONS].upsert(subscription) {
                onConflict = "installation_id"
            }
            
            fcmRepository.markAsSynced()
            Log.d(TAG, "Device token registered/updated in Supabase successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error registering device token to Supabase", e)
        }
    }

    suspend fun createWaitTimeAlert(
        portNumber: String,
        portName: String,
        crossingName: String?,
        travelMode: String,
        laneType: String,
        thresholdMinutes: Int
    ): Result<Unit> {
        // Validation
        if (travelMode !in VALID_TRAVEL_MODES) {
            return Result.failure(IllegalArgumentException("Invalid travel mode: $travelMode"))
        }
        if (laneType !in VALID_LANE_TYPES) {
            return Result.failure(IllegalArgumentException("Invalid lane type: $laneType"))
        }
        if (thresholdMinutes <= 0) {
            return Result.failure(IllegalArgumentException("Threshold must be greater than 0"))
        }

        return try {
            val installationId = fcmRepository.getOrCreateInstallationId()
            val nowIso = getCurrentIsoTimestamp()

            val alert = WaitTimeAlert(
                installation_id = installationId,
                port_number = portNumber,
                port_name = portName,
                crossing_name = crossingName,
                travel_mode = travelMode,
                lane_type = laneType,
                threshold_minutes = thresholdMinutes,
                created_at = nowIso,
                updated_at = nowIso
            )

            supabaseClient.postgrest[TABLE_WAIT_TIME_ALERTS].insert(alert)
            Log.d(TAG, "Wait time alert created successfully for port $portNumber")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating wait time alert", e)
            Result.failure(e)
        }
    }

    private fun getCurrentIsoTimestamp(): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return isoFormat.format(Date())
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Deprecated: calling the new register method instead
    suspend fun syncFcmToken(token: String) {
        registerDeviceToken(token)
    }

    fun isUserLoggedIn(): Boolean {
        return supabaseClient.auth.currentUserOrNull() != null
    }
}
