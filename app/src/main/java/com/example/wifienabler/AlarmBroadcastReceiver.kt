package com.example.wifienabler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.net.wifi.WifiManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

class AlarmBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "id1233"

        fun getPerf(context: Context): SharedPreferences? {
            return context.getSharedPreferences(context.getString(R.string.event_log_perf), Context.MODE_PRIVATE)
        }
        fun appendLog(logs : String, newOne : String) : String {
            var items = logs.split("\n")
            if (items.size > 9) {
                items = items.subList(items.size-9, items.size)
            }
            return items.joinToString("\n") + "\n" + newOne
        }
        fun logEvent(context: Context?, content: String) {
            if (context != null) {
                val perf = getPerf(context)
                val log = perf?.getString("eventLog", "")!!
                with(perf.edit()) {
                    this?.putString("eventLog", appendLog(log, content))
                    this?.apply()
                }
            }
        }
        fun getEvents(context: Context?) : String{
            return getPerf(context!!)?.getString("eventLog", "").toString()
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val curDateTime = SimpleDateFormat("MM.dd HH:mm:ss", Locale.getDefault()).format(Date())
        var text = "Turn on WiFi at " + curDateTime
        val succ = enableWiFi(context)
        text += " " + succ.toString()
        Log.i("WiFiEnablerBroadcast", text)
        logEvent(context, text)
        if(!succ && context!=null) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("WiFi")
                .setContentText("Please enable WiFi manually")
                .setContentIntent(PendingIntent.getActivity(context, 0, Intent(), 0))
                .setAutoCancel(true)
            with(NotificationManagerCompat.from(context)) {
                // notificationId is a unique int for each notification that you must define
                val notificationId = 103451
                notify(notificationId, builder.build())
            }

//            launchWiFiSetting(context)
        }

        if (context != null) {
            text = registerAlarm(context)
            Log.i("WiFiEnablerBroadcast", text)
//            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun launchWiFiSetting(context: Context?) {
//        val cn = ComponentName("com.android.settings","com.android.settings.wifi.WifiSettings")
        with(Intent(Settings.ACTION_WIFI_SETTINGS)) {
//            setComponent(cn)
            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context?.startActivity(this)
            } catch (exp: Exception) {
                Log.e("WiFiEnablerBroadcast", exp.message.toString())
            }
        }
    }

    fun enableWiFi(context: Context?) : Boolean {
        val wifiMan = context?.getApplicationContext()?.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiMan.isWifiEnabled) {
            return true
        }
        return wifiMan.setWifiEnabled(true)
    }

    fun registerAlarm(ctx: Context) : String {
        val appCtx = ctx.applicationContext
        val alarmMan = appCtx.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val intent = Intent(appCtx, AlarmBroadcastReceiver::class.java)
        val pendIntent = PendingIntent.getBroadcast(appCtx, 123, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val now = Calendar.getInstance();
        var result = "Alarm at ";
        var alarmWindow = 10 * 60 * 1000L;
        val calendar = Calendar.getInstance().apply {
//            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 17)
            set(Calendar.MINUTE, 25)
            if (now.timeInMillis-timeInMillis >= -alarmWindow) {
                // `calendar` is before `now`
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        alarmMan?.setWindow(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmWindow, pendIntent)
        result += calendar.time.toString();
        result += "\nand ";

        val pendIntent2 = PendingIntent.getBroadcast(appCtx, 124, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        with(calendar) {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 50)
            if (now.timeInMillis-timeInMillis >= -alarmWindow) {
                // `calendar` is before `now`
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        alarmMan?.setWindow(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, alarmWindow, pendIntent2)
        result += calendar.time.toString();
        return result
    }
}