package com.example.nnuzaba47.syncedjournal

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

/**
 * View Model to keep a reference to the word repository and
 * an up-to-date list of all words.
 */

class EntryViewModel : AndroidViewModel{

    private var mRepository: EntryRepository
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private var allEntries: LiveData<List<Entry>>

    constructor(application: Application) : super(application) {
        mRepository = EntryRepository(application)
        allEntries = mRepository.getAllEntries()
    }

    fun insert(entry: Entry) {
        mRepository.insert(entry)
    }

    fun getAllEntries(): LiveData<List<Entry>> {
        return allEntries
    }

    fun update(entry:Entry){
        mRepository.update(entry)
    }

    fun getById(id: Long):Entry?{
        return mRepository.getEntryById(id)
    }

    fun delete(entry:Entry){
        mRepository.delete(entry)
    }

}