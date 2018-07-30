package com.example.nnuzaba47.syncedjournal

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_enter_password.*

class EnterPasswordActivity : AppCompatActivity() {

    var password:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_password)
        var settings: SharedPreferences = getSharedPreferences("PREFS", 0)
        password = settings.getString("password", "")
    }

    fun unlock(view: View){
        var enteredPassword = etPassword.text.toString()
        if(enteredPassword.equals(password)){
            var intent: Intent = Intent(applicationContext, EntriesActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            Toast.makeText(applicationContext, "Wrong Password", Toast.LENGTH_LONG).show()
        }
    }
}
