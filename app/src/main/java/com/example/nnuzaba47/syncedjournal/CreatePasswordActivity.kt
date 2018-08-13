package com.example.nnuzaba47.syncedjournal

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_password.*

class CreatePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_password)
    }

    //Creates password when the passwords on both fields match
    fun createPassword(view: View){
        var password1 = etPassword1.text.toString()
        var password2 = etPassword2.text.toString()
        when (password1) {
            "" -> Toast.makeText(applicationContext, "Enter Password", Toast.LENGTH_LONG).show()
            password2 -> {
                var settings:SharedPreferences = getSharedPreferences("PREFS", 0)
                var editor:SharedPreferences.Editor = settings.edit();
                editor.putString("password", password1)
                editor.apply()
                Toast.makeText(applicationContext, "Success!", Toast.LENGTH_LONG).show()
                var intent = Intent(applicationContext, EnterPasswordActivity::class.java)
                startActivity(intent)
                finish()
            }
            else -> Toast.makeText(applicationContext, "Passwords must match", Toast.LENGTH_LONG).show()
        }
    }
}
