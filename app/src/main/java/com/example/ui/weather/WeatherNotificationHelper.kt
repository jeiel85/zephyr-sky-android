package com.example.ui.weather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object WeatherNotificationHelper {
    private const val WEATHER_ALERT_CHANNEL_ID = "weather_alerts"
    private const val CURRENT_TEMP_CHANNEL_ID = "current_temp"
    
    private const val CURRENT_TEMP_NOTIFICATION_ID = 2001
    private const val WEATHER_ALERT_NOTIFICATION_ID = 2002
    private const val FINE_DUST_ALERT_NOTIFICATION_ID = 2003

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameAlert = "날씨 및 미세먼지 알림"
            val descAlert = "기온 변동, 강우/강설 안내 및 미세먼지 수치 경보를 알려드립니다."
            val channelAlert = NotificationChannel(
                WEATHER_ALERT_CHANNEL_ID,
                nameAlert,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = descAlert
            }

            val nameTemp = "상단바 실시간 기온"
            val descTemp = "지정된 도시의 현재 기온 정보를 알림창에 상시 표시합니다."
            val channelTemp = NotificationChannel(
                CURRENT_TEMP_CHANNEL_ID,
                nameTemp,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = descTemp
                setShowBadge(false)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelAlert)
            notificationManager.createNotificationChannel(channelTemp)
        }
    }

    fun showCurrentTempNotification(context: Context, cityName: String, tempText: String, conditionText: String) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CURRENT_TEMP_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("상단바 현재 기온: $tempText")
            .setContentText("위치: $cityName • 현황: $conditionText")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true) // Persistent in notification drawer
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(CURRENT_TEMP_NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearCurrentTempNotification(context: Context) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(CURRENT_TEMP_NOTIFICATION_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showWeatherAlertNotification(context: Context, title: String, content: String) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Generate standard system info drawable icons safely
        val builder = NotificationCompat.Builder(context, WEATHER_ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val randomId = WEATHER_ALERT_NOTIFICATION_ID + (Math.random() * 500).toInt()
            notificationManager.notify(randomId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showFineDustAlertNotification(context: Context, title: String, content: String) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, WEATHER_ALERT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val randomId = FINE_DUST_ALERT_NOTIFICATION_ID + (Math.random() * 500).toInt()
            notificationManager.notify(randomId, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
