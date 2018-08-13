package com.example.nnuzaba47.syncedjournal.Database

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import com.example.nnuzaba47.syncedjournal.POJO.Post

class PostRepository{
    private var mPostDao: PostDao

    private var allPosts: LiveData<List<Post>>
    var mPost: Post?=null
    var mPostsForEntry:List<Post>?=null

    /**
     * @param application The current application
     */
    constructor(application: Application) {
        var db: MyDatabase? = MyDatabase.getDatabase(application.applicationContext)
        mPostDao = db!!.postDao()
        allPosts = mPostDao!!.getAll()
    }


    /**
     * Inserts into database the given post
     *
     */
    fun insert(post: Post){
        insertAsyncTask(mPostDao).execute(post)
    }

    fun update(post: Post){
        UpdateAsyncTask(mPostDao).execute(post)
    }

    fun getAllPosts(): LiveData<List<Post>> {
        return allPosts
    }

    fun getPostById(id: Int): Post?{
        LoadPostAsyncTask(mPostDao).execute(id)
        return mPost
    }

    fun getPostsByEntryId(entryId: Long):List<Post>? {
        return mPostDao.loadPostsForEntry(entryId)
    }




    inner class insertAsyncTask internal constructor(private val mAsyncTaskDao: PostDao) : AsyncTask<Post, Void, Void>() {

        override fun doInBackground(vararg params: Post): Void? {
            mAsyncTaskDao.insert(params[0])
            return null
        }
    }

    inner class UpdateAsyncTask internal constructor(private val mAsyncTaskDao: PostDao) : AsyncTask<Post, Void, Void>() {

        override fun doInBackground(vararg params: Post): Void? {
            mAsyncTaskDao.update(params[0])
            return null
        }
    }

    inner class LoadPostAsyncTask internal constructor(private val mAsyncTaskDao: PostDao) : AsyncTask<Int, Void, Void>() {
        override fun doInBackground(vararg params: Int?): Void? {
            mPost = mAsyncTaskDao.loadById(params[0])
            return null
        }
    }

}

