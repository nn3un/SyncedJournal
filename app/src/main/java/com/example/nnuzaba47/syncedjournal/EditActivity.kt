package com.example.nnuzaba47.syncedjournal

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_edit.view.*
import kotlinx.android.synthetic.main.activity_show.*

class EditActivity : AppCompatActivity() {

    var entryId:Int = -1
    var entry:Entry?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        entryId = intent.getIntExtra("id", -1)
        if(entryId == -1){
            startActivity(Intent(applicationContext, EntriesActivity::class.java))
            Toast.makeText(applicationContext, "Error getting data from database", Toast.LENGTH_LONG).show()
            finish()
        }
        else{
            //var entry:Entry? = ViewModelProviders.of(this).get(EntryViewModel::class.java).getById(entryId)
            entry = EntryDatabase.getDatabase(applicationContext)!!.entryDao().loadById(entryId)
            if (entry != null){
                etEditEntryTitle.setText(entry!!.title)
                etEditEntryDescription.setText(entry!!.description)
            }
            else{
                startActivity(Intent(applicationContext, EntriesActivity::class.java))
                Toast.makeText(applicationContext, "Failed to load entry :(", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    fun updateEntry(view: View){
        entry!!.title = etEditEntryTitle.text.toString()
        entry!!.description = etEditEntryDescription.text.toString()
        EntryDatabase.getDatabase(applicationContext)!!.entryDao().update(entry!!)
        var intent = Intent(applicationContext, ShowActivity::class.java)
        intent.putExtra("entryId", entry!!.id)
        startActivity(intent)
        finish()
    }

}
