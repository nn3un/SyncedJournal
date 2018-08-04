package com.example.nnuzaba47.syncedjournal

import android.arch.lifecycle.Observer
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_edit.*

class EditActivity : AppCompatActivity() {

    var entryId:Long = -1
    var entry:Entry?= null
    private var mPostViewModel:PostViewModel ?= null
    private var adapter: PostAdapterForEditActivity ?= null
    var postDao: PostDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        //Set up
        mPostViewModel = PostViewModel(application)
        postDao = MyDatabase.getDatabase(applicationContext)!!.postDao()

        entryId = intent.getLongExtra("id", -1)
        if(entryId <  0){
            startActivity(Intent(applicationContext, EntriesActivity::class.java))
            Toast.makeText(applicationContext, "Error getting data from database", Toast.LENGTH_LONG).show()
            finish()
        }
        else{
            //var entry:Entry? = ViewModelProviders.of(this).get(EntryViewModel::class.java).getById(entryId)
            entry = MyDatabase.getDatabase(applicationContext)!!.entryDao().loadById(entryId)
            if (entry != null){
                etEditEntryTitle.setText(entry!!.title)
                etEditEntryDescription.setText(entry!!.description)
                adapter = PostAdapterForEditActivity(this)

                var postsLiveData = mPostViewModel!!.getAllPostsForEntryId(entry!!.id!!)
                postsLiveData.observe(this, Observer<List<Post>>{
                    adapter!!.setPosts(ArrayList(it!!))
                })

                rvEditPosts.adapter = adapter
                rvEditPosts.layoutManager = LinearLayoutManager(this)
            }
            else{
                startActivity(Intent(applicationContext, EntriesActivity::class.java))
                Toast.makeText(applicationContext, "Failed to load entry :(", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    fun updateEntry(view: View){
        //update the entry information
        entry!!.title = etEditEntryTitle.text.toString()
        entry!!.description = etEditEntryDescription.text.toString()
        MyDatabase.getDatabase(applicationContext)!!.entryDao().update(entry!!)

        //Update the post's information, namely the description field
        adapter!!.items!!.forEach {
            var etEditPostDescription = adapter!!.postToDescriptionMap[it]
            if(it.description != etEditPostDescription!!.text.toString()){
                it.description = etEditPostDescription!!.text.toString()
                postDao!!.update(it)
            }
        }

        var intent = Intent(applicationContext, ShowActivity::class.java)
        intent.putExtra("entryId", entry!!.id)
        startActivity(intent)
        finish()
    }

}
