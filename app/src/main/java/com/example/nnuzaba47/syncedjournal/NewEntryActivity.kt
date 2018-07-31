package com.example.nnuzaba47.syncedjournal

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_new_entry.*
import java.sql.Date
import java.util.*

class NewEntryActivity : AppCompatActivity() {

    private var entryViewModel:EntryViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_entry)
        entryViewModel = ViewModelProviders.of(this).get(EntryViewModel::class.java)

    }

    fun addEntry(view: View){
        val replyIntent = Intent()
        if (TextUtils.isEmpty(etEditEntryTitle.text) || TextUtils.isEmpty(etEditEntryDescription.text)) {
            Toast.makeText(applicationContext, "Please fill out the fields", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(applicationContext, "Added successfully", Toast.LENGTH_LONG).show()
            val title = etEditEntryTitle.text.toString()
            val description = etEditEntryDescription.text.toString()
            val entry = Entry(title, description, Date())
            entryViewModel!!.insert(entry)
            finish()
        }

    }
}
