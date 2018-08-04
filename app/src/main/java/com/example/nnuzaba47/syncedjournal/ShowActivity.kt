package com.example.nnuzaba47.syncedjournal

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_show.*

class ShowActivity : AppCompatActivity() {

    var entryId:Long ?= null  //The entryId
    var mPostViewModel:PostViewModel ?= null
    var adapter: PostAdapterForShowActivity ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)
        mPostViewModel = PostViewModel(application) //Setup postViewModel

        //Get entryId from EntriesActivity
        entryId = intent.getLongExtra("entryId", -1)

        //If the entry Id wasn't in the intent go back to the EntriesActivity
        if(entryId!! < 0){
            startActivity(Intent(applicationContext, EntriesActivity::class.java))
            Toast.makeText(applicationContext, "Entry Not found", Toast.LENGTH_LONG).show()
            finish()
        }
        else{
            var entry:Entry? = MyDatabase.getDatabase(applicationContext)!!.entryDao().loadById(entryId)
            //if there was an entry found, we can go ahead and show the informations
            if (entry != null){
                //Set the title and description textViews
                etShowEntryTitle.text = entry.title
                etShowEntryDescription.text = entry.description
                //Initiate the adapter and start observing the live data
                adapter = PostAdapterForShowActivity(this)
                mPostViewModel!!.getAllPostsForEntryId(entry.id!!).observe(this, Observer<List<Post>>{
                    adapter!!.setPosts(ArrayList(it!!))
                })
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
        startActivity(intent)
        finish()
    }

    fun goBackToEntries(view: View){
        var intent = Intent(applicationContext, EntriesActivity::class.java)
        startActivity(intent)
        finish()
    }

}
