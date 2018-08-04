package com.example.nnuzaba47.syncedjournal


import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_entries.*
import java.util.*

class EntriesActivity : AppCompatActivity() {

    var context: Context =this

    private var mEntryViewModel:EntryViewModel?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entries)

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
        finish()
    }



    fun showEntry(view: View){
        var viewHolder = rvEntries.findContainingViewHolder(view)
        var position:Int = viewHolder!!.adapterPosition
        var adapter = rvEntries.adapter as MyEntryAdapter
        var entry = adapter.getEntryAtPosition(position)
        var intent = Intent(applicationContext, ShowActivity::class.java)
        intent.putExtra("entryId", entry!!.id)
        startActivity(intent)
        finish()
    }
}