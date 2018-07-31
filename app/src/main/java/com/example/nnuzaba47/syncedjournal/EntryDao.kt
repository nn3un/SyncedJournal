package com.example.nnuzaba47.syncedjournal

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface EntryDao {
    @Query("SELECT * FROM entryTable")
    fun getAll():LiveData<List<Entry>>
    @Query("SELECT * FROM entryTable WHERE id IN (:entryIds)")
    fun loadAllByIds(entryIds:IntArray):List<Entry>
    @Query("SELECT * FROM entryTable WHERE id = (:entryId)")
    fun loadById(entryId: Int?):Entry
    @Insert
    fun insertAll(vararg entries:Entry)
    @Insert
    fun insert(entry:Entry)
    @Delete
    fun delete(entry:Entry)
    @Update
    fun update(entry:Entry)
}