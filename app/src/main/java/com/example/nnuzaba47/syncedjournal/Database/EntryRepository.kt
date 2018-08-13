package com.example.nnuzaba47.syncedjournal.Database

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import com.example.nnuzaba47.syncedjournal.POJO.Entry

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 *
 */

class EntryRepository{
    private var mEntryDao: EntryDao
    private var allEntries: LiveData<List<Entry>>
    var mEntry: Entry?=null

    /**
     * @param application The current application
     */
    constructor(application: Application) {
        var db: MyDatabase?= MyDatabase.getDatabase(application.applicationContext)
        mEntryDao = db!!.entryDao()
        allEntries = mEntryDao!!.getAll()
    }


    /**
     * Inserts into database the given entry
     */
    fun insert(entry: Entry) {
        InsertAsyncTask(mEntryDao).execute(entry)
    }

    fun update(entry: Entry){
        UpdateAsyncTask(mEntryDao).execute(entry)
    }

    fun delete(entry: Entry){
        DeleteAsyncTask(mEntryDao).execute(entry)
    }

    fun getAllEntries(): LiveData<List<Entry>> {
        return allEntries
    }

    //Asynchronous classes
    companion object {
        class InsertAsyncTask internal constructor(private val mAsyncTaskDao: EntryDao) : AsyncTask<Entry, Void, Void>() {
            override fun doInBackground(vararg params: Entry): Void? {
                mAsyncTaskDao.insert(params[0])
                return null
            }
        }

        class UpdateAsyncTask internal constructor(private val mAsyncTaskDao: EntryDao) : AsyncTask<Entry, Void, Void>() {
            override fun doInBackground(vararg params: Entry): Void? {
                mAsyncTaskDao.update(params[0])
                return null
            }
        }

        class DeleteAsyncTask internal constructor(private val mAsyncTaskDao: EntryDao) : AsyncTask<Entry, Void, Void>() {
            override fun doInBackground(vararg params: Entry): Void? {
                mAsyncTaskDao.delete(params[0])
                return null
            }
        }
    }

}

