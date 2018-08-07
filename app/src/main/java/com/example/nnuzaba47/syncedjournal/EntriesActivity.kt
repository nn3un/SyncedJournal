package com.example.nnuzaba47.syncedjournal


import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.facebook.login.LoginManager
import kotlinx.android.synthetic.main.activity_entries.*
import java.util.*

class EntriesActivity : AppCompatActivity() {

    var context: Context =this

    private var mEntryViewModel:EntryViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entries)

        setSupportActionBar(toolbar)
        var adapter = MyEntryAdapter(this)
        rvEntries.adapter = adapter
        rvEntries.layoutManager = LinearLayoutManager(this)

        mEntryViewModel = ViewModelProviders.of(this).get(EntryViewModel::class.java)

        mEntryViewModel!!.getAllEntries().observe(this, Observer<List<Entry>>{
            adapter.setEntries(it!!)
        })
    }

    fun newEntry(view: View){
        startActivity(Intent(applicationContext, NewEntryActivity::class.java))
    }



    fun showEntry(view: View){
        var viewHolder = rvEntries.findContainingViewHolder(view)
        var position:Int = viewHolder!!.adapterPosition
        var adapter = rvEntries.adapter as MyEntryAdapter
        var entry = adapter.getEntryAtPosition(position)
        var intent = Intent(applicationContext, ShowActivity::class.java)
        intent.putExtra("entryId", entry!!.id)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            LoginManager.getInstance().logOut()
            true
        }

        R.id.action_changePassword -> {
            var intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}