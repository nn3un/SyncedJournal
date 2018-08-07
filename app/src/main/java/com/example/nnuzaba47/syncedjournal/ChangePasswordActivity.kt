package com.example.nnuzaba47.syncedjournal

import android.content.Intent
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_change_password.*

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)
    }

    fun changePassword(view: View){

        var settings: SharedPreferences = getSharedPreferences("PREFS", 0)
        var password = settings.getString("password", "")
        var enteredPassword = etOldPassword.text.toString()
        if(enteredPassword == password){
            var newPassword1 = etNewPassword1.text.toString()
            var newPassword2 = etNewPassword2.text.toString()
            if(newPassword1.equals("")){
                Toast.makeText(applicationContext, "Enter Password", Toast.LENGTH_LONG).show()
            }
            else if(newPassword1.equals(newPassword2)){
                var settings: SharedPreferences = getSharedPreferences("PREFS", 0)
                var editor: SharedPreferences.Editor = settings.edit();
                editor.putString("password", newPassword1)
                editor.apply()
                Toast.makeText(applicationContext, "Success!", Toast.LENGTH_LONG).show()
                var intent = Intent(applicationContext, EnterPasswordActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(applicationContext, "Passwords must match", Toast.LENGTH_LONG).show()
            }
        }
        else{
            Toast.makeText(applicationContext, "Wrong Password", Toast.LENGTH_LONG).show()
        }

    }
    }

