package com.example.nnuzaba47.syncedjournal

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface EntryDao {
    @Query("SELECT * FROM entryTable ORDER BY date DESC")
    fun getAll():LiveData<List<Entry>>
    @Query("SELECT * FROM entryTable WHERE id IN (:entryIds)")
    fun loadAllByIds(entryIds:LongArray):List<Entry>
    @Query("SELECT * FROM entryTable WHERE id = (:entryId)")
    fun loadById(entryId: Long?):Entry
    @Insert
    fun insertAll(vararg entries:Entry)
    @Insert
    fun insert(entry:Entry):Long
    @Delete
    fun delete(entry:Entry)
    @Update
    fun update(entry:Entry)
}