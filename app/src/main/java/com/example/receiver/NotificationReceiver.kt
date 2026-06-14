package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "emotionai_reminders"
        
        // Ensure channel is registered
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "EmotionAI Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily wellness checks and mood tracking reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val type = intent.getStringExtra("REMINDER_TYPE") ?: "DAILY"
        val title = if (type == "DAILY") {
            "Daily Wellness Tracker"
        } else {
            "Mindful Check-in"
        }
        val message = if (type == "DAILY") {
            "Take 1 minute to log your thoughts and receive a real-time stress assessment."
        } else {
            "How are you feeling right now? Tap to explore recommendations."
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            if (type == "DAILY") 101 else 102,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(if (type == "DAILY") 1 else 2, notification)
    }

    companion object {
        fun triggerInstantReminder(context: Context, type: String) {
            // High-fidelity instant trigger for testing
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("REMINDER_TYPE", type)
            }
            context.sendBroadcast(intent)
        }
    }
}
