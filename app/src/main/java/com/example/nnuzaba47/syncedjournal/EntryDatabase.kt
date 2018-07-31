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


@Database(entities = [(Entry::class)], version = 2)
@TypeConverters(Converters::class)
abstract class EntryDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao



    companion object {
        private var INSTANCE: EntryDatabase? = null

        fun getDatabase(context: Context): EntryDatabase? {
            if (INSTANCE == null) {
                synchronized(EntryDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                EntryDatabase::class.java, "entry_database").allowMainThreadQueries()
                                // Wipes and rebuilds instead of migrating if no Migration object.
                                // Migration is not part of this codelab.
                                .fallbackToDestructiveMigration()
                                .build()
                    }
                }
            }
            return INSTANCE
        }

        /**
         * Override the onOpen method to populate the database.
         * For this sample, we clear the database every time it is created or opened.
         */

    }

    }


