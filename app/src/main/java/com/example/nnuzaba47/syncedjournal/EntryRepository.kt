package com.example.nnuzaba47.syncedjournal

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask

/**
 * Abstracted Repository as promoted by the Architecture Guide.
 *
 */

class EntryRepository{
    private var mEntryDao: EntryDao
    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.

    private var allEntries: LiveData<List<Entry>>
    var mEntry:Entry?=null

    /**
     * @param application The current application
     */
    constructor(application: Application) {
        var db:EntryDatabase ?= EntryDatabase.getDatabase(application.applicationContext)
        mEntryDao = db!!.entryDao()
        allEntries = mEntryDao!!.getAll()
    }


    /**
     * Inserts into database the given entry
     */
    fun insert(entry: Entry) {
        insertAsyncTask(mEntryDao).execute(entry)
    }

    fun update(entry: Entry){
        updateAsyncTask(mEntryDao).execute(entry)
    }

    fun delete(entry: Entry){
        deleteAsyncTask(mEntryDao).execute(entry)
    }

    fun getAllEntries(): LiveData<List<Entry>> {
        return allEntries
    }

    fun getEntryById(id: Int): Entry?{
        loadEntryAsyncTask(mEntryDao).execute(id)
        return mEntry
    }




    inner class insertAsyncTask internal constructor(private val mAsyncTaskDao: EntryDao) : AsyncTask<Entry, Void, Void>() {

        override fun doInBackground(vararg params: Entry): Void? {
            mAsyncTaskDao.insert(params[0])
            return null
        }
    }

    inner class updateAsyncTask internal constructor(private val mAsyncTaskDao: EntryDao) : AsyncTask<Entry, Void, Void>() {

        override fun doInBackground(vararg params: Entry): Void? {
            mAsyncTaskDao.update(params[0])
            return null
        }
    }

    inner class loadEntryAsyncTask internal constructor(private val mAsyncTaskDao: EntryDao) : AsyncTask<Int, Void, Void>() {
        override fun doInBackground(vararg params: Int?): Void? {
            mEntry = mAsyncTaskDao.loadById(params[0])
            return null
        }

        override fun onPostExecute(result: Void?) {

        }

    }

    inner class deleteAsyncTask internal constructor(private val mAsyncTaskDao: EntryDao) : AsyncTask<Entry, Void, Void>() {

        override fun doInBackground(vararg params: Entry): Void? {
            mAsyncTaskDao.delete(params[0])
            return null
        }


    }

    }

