package com.example.nnuzaba47.syncedjournal

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.nnuzaba47.syncedjournal.Adapter.PostAdapterForShowActivity
import com.example.nnuzaba47.syncedjournal.Database.EntryDao
import com.example.nnuzaba47.syncedjournal.Database.MyDatabase
import com.example.nnuzaba47.syncedjournal.Database.PostDao
import com.example.nnuzaba47.syncedjournal.Database.PostViewModel
import com.example.nnuzaba47.syncedjournal.POJO.Entry
import kotlinx.android.synthetic.main.activity_show.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ShowActivity : AppCompatActivity() {
    private var entryId:Long ?= null
    private var entry: Entry?= null
    private var entryDao: EntryDao?= null

    private var mPostViewModel: PostViewModel?= null
    private var adapter: PostAdapterForShowActivity?= null
    private var postDao: PostDao?= null

    companion object {
        const val REQUEST_CODE_FOR_EDIT_ACTIVITY = 0
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show)

        entryDao = MyDatabase.getDatabase(applicationContext)!!.entryDao()
        postDao = MyDatabase.getDatabase(applicationContext)!!.postDao()
        mPostViewModel = PostViewModel(application)

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
                setSupportActionBar(tbShowEntry)
                if(supportActionBar!=null){
                    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                }
                //Set the title and description textViews
                tvShowEntryTitle.text = entry!!.title
                tvShowEntryDescription.text = entry!!.description
                tbShowEntry.title = "Entry for " + SimpleDateFormat("MMM d, yyyy", Locale.US).format(entry!!.date)
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
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FOR_EDIT_ACTIVITY){
            entryId = data!!.getLongExtra("entryId", -1)
        }
    }

    override fun onResume() {
        super.onResume()
        entry = entryDao!!.loadById(entryId)
        tvShowEntryTitle.text = entry!!.title
        tvShowEntryDescription.text = entry!!.description
        tbShowEntry.title = "Entry for " + SimpleDateFormat("MMM d, yyyy", Locale.US).format(entry!!.date)
        //Initiate the adapter and start observing the live data
        adapter!!.setPosts(ArrayList(postDao!!.loadPostsForEntry(entryId!!)))
        adapter!!.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_show_activity, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        //Start Edit Activity
        R.id.action_edit -> {
            var intent = Intent(applicationContext, EditActivity::class.java)
            intent.putExtra("id", entryId)
            startActivityForResult(intent, REQUEST_CODE_FOR_EDIT_ACTIVITY)
            true
        }

        //Delete Entry
        R.id.action_delete -> {
            var entryDao = MyDatabase.getDatabase(applicationContext)!!.entryDao()
            var entry: Entry? = entryDao.loadById(entryId)
            if (entry != null) {
                entryDao.delete(entry)
            }
            Toast.makeText(applicationContext, "Deleted", Toast.LENGTH_LONG).show()
            finish()
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }



}
