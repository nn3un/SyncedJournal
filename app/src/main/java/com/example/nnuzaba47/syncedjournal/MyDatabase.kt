package com.example.nnuzaba47.syncedjournal

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase.Callback
import android.arch.persistence.room.TypeConverters
import android.content.Context
import android.os.AsyncTask
import java.util.*


@Database(entities = [(Entry::class), (Post::class)], version = 13, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MyDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao
    abstract fun postDao(): PostDao

    companion object {
        private var INSTANCE: MyDatabase? = null

        fun getDatabase(context: Context): MyDatabase? {
            if (INSTANCE == null) {
                synchronized(MyDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                MyDatabase::class.java, "database").allowMainThreadQueries()
                                .fallbackToDestructiveMigration()
                                .build()
                    }
                }
            }
            return INSTANCE
        }
    }
    }


