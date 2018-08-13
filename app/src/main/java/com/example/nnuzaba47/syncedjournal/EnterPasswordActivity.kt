package com.example.nnuzaba47.syncedjournal

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_enter_password.*
import java.util.*

class EnterPasswordActivity : AppCompatActivity() {

    var password:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_password)
        var settings: SharedPreferences = getSharedPreferences("PREFS", 0)
        password = settings.getString("password", "")
        setUpNotification()
    }

    //Unlock if password correct
    fun unlock(view: View){
        var enteredPassword = etPassword.text.toString()
        if(enteredPassword == password){
            var intent: Intent = Intent(applicationContext, EntriesActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            Toast.makeText(applicationContext, "Wrong Password", Toast.LENGTH_LONG).show()
        }
    }

    //Set repeating notifications everyday at 8 pm
    private fun setUpNotification(){
        val myIntent = Intent(applicationContext, MyBroadcastReceiver::class.java)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(this, 0, myIntent, 0)

        var calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR, 8)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.AM_PM, Calendar.PM)

        alarmManager.setRepeating(AlarmManager.RTC, calendar.timeInMillis, 24*60*60*1000, pendingIntent)
    }


}
