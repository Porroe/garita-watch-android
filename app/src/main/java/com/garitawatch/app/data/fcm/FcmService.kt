package com.garitawatch.app.data.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.garitawatch.app.MainActivity
import com.garitawatch.app.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenManager: FcmTokenManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        serviceScope.launch {
            fcmTokenManager.updateStoredToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "From: ${message.from}")

        // Handle data payload
        if (message.data.isNotEmpty()) {
            handleNotificationPayload(message.data)
        }
        
        // Handle notification payload (if any)
        message.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    private fun handleNotificationPayload(data: Map<String, String>) {
        val portName = data["port_name"] ?: "Unknown Port"
        val crossingName = data["crossing_name"]
        val travelMode = data["travel_mode"] ?: ""
        val laneType = data["lane_type"] ?: ""
        val observedDelay = data["observed_delay_minutes"] ?: "0"
        val portNumber = data["port_number"]

        val locationInfo = if (crossingName.isNullOrEmpty()) portName else "$portName ($crossingName)"
        val modeInfo = if (travelMode.isNotEmpty() && laneType.isNotEmpty()) {
            val formattedMode = travelMode.lowercase().replaceFirstChar { it.uppercase() }
            val formattedLane = laneType.lowercase().replaceFirstChar { it.uppercase() }
            " ($formattedMode - $formattedLane)"
        } else ""

        val body = "Wait time alert: $locationInfo$modeInfo is now $observedDelay min"

        showNotification(body, portNumber)
    }

    private fun showNotification(body: String, portNumber: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wait Time Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for wait time threshold alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (portNumber != null) {
                putExtra("port_number", portNumber)
            }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Using launcher icon as fallback
            .setContentTitle("Wait Time Alert")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val TAG = "FcmService"
        private const val CHANNEL_ID = "wait_time_alerts"
    }
}
