package com.example.nnuzaba47.syncedjournal

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.ForeignKey.CASCADE
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "postTable", foreignKeys = [ForeignKey(entity = Entry::class, parentColumns = arrayOf("id"), childColumns = arrayOf("entryId"), onDelete = CASCADE)])
class Post {
    @PrimaryKey(autoGenerate = true)
    var postId: Long ?= null
    var sourceURL: String?
    var description: String?
    var imageURL: String?
    var entryId: Long?= null


    constructor(sourceURL:String, description: String, imageURL:String){
        this.sourceURL = sourceURL
        this.description = description
        this.imageURL = imageURL
    }
}