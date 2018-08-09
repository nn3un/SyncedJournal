package com.example.nnuzaba47.syncedjournal

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.app.NotificationManager
import android.app.NotificationChannel
import android.os.Build
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.icu.util.DateInterval
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import java.util.*
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_splash.*


class SplashActivity : AppCompatActivity() {

    var password:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // on some click or some loading we need to wait for...
        val pb = progressBar as ProgressBar
        pb.visibility = ProgressBar.VISIBLE

        var settings:SharedPreferences = getSharedPreferences("PREFS", 0)
        password = settings.getString("password", "")

        var handler:Handler = Handler()
        handler.postDelayed(object: Runnable{
            override fun run(){
                if(password.equals("")){
                    var intent: Intent = Intent(applicationContext, CreatePasswordActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    var intent: Intent = Intent(applicationContext, EnterPasswordActivity::class.java)
                    startActivity(intent)
                    finish()
                }

            }
        }, 2000)
    }
}
