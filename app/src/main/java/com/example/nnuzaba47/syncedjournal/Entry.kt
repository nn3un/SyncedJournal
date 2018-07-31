package com.example.nnuzaba47.syncedjournal

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.util.EventLogTags
import java.util.*


@Entity(tableName = "entryTable")
class Entry {
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0
    var title:String?=null
    var description:String?=null
    var date:Date ?=null

    constructor(title:String, description: String, date:Date){
        this.title = title
        this.description = description
        this.date = date
    }

}
