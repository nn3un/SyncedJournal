package com.example.nnuzaba47.syncedjournal

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_show.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ShowActivity : AppCompatActivity() {

    var entryId:Long ?= null  //The entryId
    var entry:Entry ?= null
    var mPostViewModel:PostViewModel ?= null
    var adapter: PostAdapterForShowActivity ?= null
    var postDao:PostDao ?= null
    var entryDao:EntryDao ?= null
    companion object {
        const val REQUEST_CODE_FOR_EDIT_ACTIVITY = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("tag", "Show activity created")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)
        mPostViewModel = PostViewModel(application) //Setup postViewModel
        postDao = MyDatabase.getDatabase(applicationContext)!!.postDao()
        entryDao = MyDatabase.getDatabase(applicationContext)!!.entryDao()
        //Get entryId from EntriesActivity
        if (entryId == null) {
            entryId = intent.getLongExtra("entryId", -1)
        }
        //If the entry Id wasn't in the intent or savedInstance go back to the EntriesActivity
        if(entryId!! < 0){
            startActivity(Intent(applicationContext, EntriesActivity::class.java))
            Toast.makeText(applicationContext, "Entry Not found", Toast.LENGTH_LONG).show()
            finish()
        }
        else{
            entry = MyDatabase.getDatabase(applicationContext)!!.entryDao().loadById(entryId)
            //if there was an entry found, we can go ahead and show the informations
            if (entry != null){
                //Set the title and description textViews
                tvShowEntryTitle.text = entry!!.title
                tvShowEntryDescription.text = entry!!.description
                tvShowEntryDate.text = SimpleDateFormat("MM/dd/yy", Locale.US).format(entry!!.date)
                //Initiate the adapter and start observing the live data
                adapter = PostAdapterForShowActivity(this)
                adapter!!.setPosts(ArrayList(postDao!!.loadPostsForEntry(entryId!!)))
                //Setup the recyclerView
                rvShowPosts.adapter = adapter
                rvShowPosts.layoutManager = LinearLayoutManager(this)
            }
            //if the entry isn't found we go back to EntriesActivity
            else{
                startActivity(Intent(applicationContext, EntriesActivity::class.java))
                Toast.makeText(applicationContext, "Failed to load entry :(", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("tag", "OnActivityResult called")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FOR_EDIT_ACTIVITY){
            entryId = data!!.getLongExtra("entryId", -1)
        }
    }

    override fun onStart(){
        super.onStart()
        Log.i("tag", "Show activity started")
    }
    override fun onResume() {
        super.onResume()
        Log.i("tag", "Show Activity Resumed")
        entry = entryDao!!.loadById(entryId)
        tvShowEntryTitle.text = entry!!.title
        tvShowEntryDescription.text = entry!!.description
        tvShowEntryDate.text = SimpleDateFormat("MM/dd/yy", Locale.US).format(entry!!.date)
        //Initiate the adapter and start observing the live data
        adapter!!.setPosts(ArrayList(postDao!!.loadPostsForEntry(entryId!!)))
        adapter!!.notifyDataSetChanged()
    }


    /**
     * Deletes current activity
     * @param view The current view
     * @return Void
     */
    fun deleteEntry(view: View){
        var entryDao = MyDatabase.getDatabase(applicationContext)!!.entryDao()
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
        startActivityForResult(intent, REQUEST_CODE_FOR_EDIT_ACTIVITY)
        //finish()
    }

    fun goBackToEntries(view: View){
        var intent = Intent(applicationContext, EntriesActivity::class.java)
        startActivity(intent)
        finish()
    }

}
