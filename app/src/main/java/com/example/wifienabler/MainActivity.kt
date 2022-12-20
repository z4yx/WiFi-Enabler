package com.example.wifienabler

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.getSystemService
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createNotificationChannel()

//        registerAlarm()
//        enableWiFi()
        refershEventView(findViewById<TextView>(R.id.eventsView))
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(AlarmBroadcastReceiver.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun onClickTrunOnWiFi(view: View) {
        val recv = AlarmBroadcastReceiver()
        recv.onReceive(this, null)
    }

    fun registerAlarm(view: View) {
        val recv = AlarmBroadcastReceiver()
        val text = recv.registerAlarm(this)

        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    fun refershEventView(view: View) {
        val events = AlarmBroadcastReceiver.getEvents(this)
        (view as TextView).setText(events)
    }
}