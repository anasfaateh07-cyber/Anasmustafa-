package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.R

object ParentNotificationHelper {

    private const val CHANNEL_ID = "al_hadid_parent_updates"
    private const val CHANNEL_NAME = "Al Hadid Parent Sabaq & Marks Alerts"

    fun sendParentChildUpdateNotification(
        context: Context,
        studentName: String,
        title: String,
        message: String
    ) {
        runCatching {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Instant notifications when a teacher adds Sabaq or Marks for your child"
                }
                manager.createNotificationChannel(channel)
            }

            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

            if (hasPermission) {
                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .build()

                NotificationManagerCompat.from(context).notify(
                    (studentName.hashCode() and 0x7FFFFFFF) % 10000 + 2000,
                    notification
                )
            }
            Toast.makeText(context, title, Toast.LENGTH_SHORT).show()
        }
    }
}
