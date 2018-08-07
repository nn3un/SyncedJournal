package com.example.nnuzaba47.syncedjournal

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface PostDao {
    @Query("SELECT * FROM postTable")
    fun getAll(): LiveData<List<Post>>
    @Query("SELECT * FROM postTable WHERE postId IN (:postIds)")
    fun loadAllByIds(postIds:IntArray):List<Post>
    @Query("SELECT * FROM postTable WHERE postId = (:postId)")
    fun loadById(postId: Int?):Post
    @Query("SELECT * FROM postTable WHERE entryId=:entryId")
    fun loadPostsForEntry(entryId: Long): List<Post>
    @Insert
    fun insert(post:Post):Long
    @Delete
    fun delete(post:Post)
    @Update
    fun update(post:Post)
}