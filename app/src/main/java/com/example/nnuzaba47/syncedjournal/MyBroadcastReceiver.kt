package com.example.nnuzaba47.syncedjournal

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.os.Build
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent



class MyBroadcastReceiver: BroadcastReceiver() {
    private val channelID = "com.example.syncedJournal.channelId"

    override fun onReceive(context: Context, sourceIntent: Intent) {
        //Create intent that will start the activity
        val intent = Intent(context, SplashActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val builder = Notification.Builder(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    channelID,
                    "NotificationDemo",
                    IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        //Set up notification
        val notification = builder.setContentTitle("Synced Journal Notification")
                .setContentText("Ready to jot down your day??")
                .setTicker("Reminder!!")
                .setSmallIcon(R.drawable.ic_book_black_24dp)
                .setContentIntent(pendingIntent).build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(channelID)
        }

        notificationManager.notify(0, notification)
    }
}