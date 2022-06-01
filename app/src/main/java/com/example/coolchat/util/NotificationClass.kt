package com.example.coolchat.util

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.coolchat.R
import com.example.coolchat.ui.chat.ChatActivity
import com.example.coolchat.ui.user.UserActivity

class NotificationClass(private val context: Context)
{
    var notificationBuilder: NotificationCompat.Builder

    init {
        val intent = Intent(context, UserActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE)
            }
        notificationBuilder =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, Channel_ID)
//        } else {
//            NotificationCompat.Builder(context)
//        }
        notificationBuilder.apply {
            setSmallIcon(R.mipmap.ic_launcher_foreground)
            setContentTitle("content rest")
            setContentText("content text")
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            setSilent(true)
            setFullScreenIntent(pendingIntent, true)


        }
    }
    fun showNotification() {
        println("notifiy")
        with(NotificationManagerCompat.from(context)) {
            try {
                notify(notification_ID, notificationBuilder.build())
            }catch (e:Exception) {
                println(e.message.toString())
            }
        }
    }

    companion object {
        const val Channel_ID: String = "BASE_CHANNEL_ID-X)("
        const val notification_ID = 0xb358
    }

}