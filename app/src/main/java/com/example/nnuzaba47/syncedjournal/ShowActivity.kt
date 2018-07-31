package com.example.nnuzaba47.syncedjournal

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_show.*

class ShowActivity : AppCompatActivity() {

    var entryId:Int = 0
    companion object {
        var EDIT_ENTRY_ACTIVITY_REQUEST_CODE = 2
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)
        entryId = intent.getIntExtra("entryId", -1)
        if(entryId == -1){
            startActivity(Intent(applicationContext, EntriesActivity::class.java))
            Toast.makeText(applicationContext, "Entry Not found", Toast.LENGTH_LONG).show()
            finish()
        }
        else{

            //var entry:Entry? = ViewModelProviders.of(this).get(EntryViewModel::class.java).getById(entryId)
            var entry:Entry? = EntryDatabase.getDatabase(applicationContext)!!.entryDao().loadById(entryId)
            if (entry != null){
                etShowEntryTitle.text = entry!!.title
                etShowEntryDescription.text = entry!!.description
            }
            else{
                startActivity(Intent(applicationContext, EntriesActivity::class.java))
                Toast.makeText(applicationContext, "Failed to load entry :(", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    }

    /**
     * Deletes current activity
     * @param view The current view
     * @return Void
     */
    fun deleteEntry(view: View){
        var entryDao = EntryDatabase.getDatabase(applicationContext)!!.entryDao()
        var entry:Entry? = entryDao.loadById(entryId)
        if (entry != null) {
            entryDao.delete(entry)
        }
        startActivity(Intent(applicationContext, EntriesActivity::class.java))
        Toast.makeText(applicationContext, "Deleted", Toast.LENGTH_LONG).show()
        finish()
    }

    fun editEntry(view: View){
        var intent = Intent(applicationContext, EditActivity::class.java)
        intent.putExtra("id", entryId)
        startActivity(intent)
        finish()
    }

    fun goBackToEntries(view: View){
        var intent = Intent(applicationContext, EntriesActivity::class.java)
        startActivity(intent)
        finish()
    }

}
