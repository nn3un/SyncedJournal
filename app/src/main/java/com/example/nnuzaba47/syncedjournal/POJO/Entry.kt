package com.example.nnuzaba47.syncedjournal.POJO

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import android.util.EventLogTags
import java.util.*
import kotlin.collections.ArrayList


@Entity(tableName = "entryTable", indices = [(Index("id"))])
class Entry {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id:Long ?= null
    var title:String? = null
    var description:String? = null
    var date:Date? = null

    constructor(title:String, description: String, date:Date)  {
        this.title = title
        this.description = description
        this.date = date
    }

}
