package com.example.nnuzaba47.syncedjournal

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {

    var password:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

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
