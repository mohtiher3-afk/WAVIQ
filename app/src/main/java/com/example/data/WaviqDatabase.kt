package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MasterTrackEntity::class], version = 1, exportSchema = false)
abstract class WaviqDatabase : RoomDatabase() {
    abstract fun masterTrackDao(): MasterTrackDao

    companion object {
        @Volatile
        private var INSTANCE: WaviqDatabase? = null

        fun getInstance(context: Context): WaviqDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    WaviqDatabase::class.java,
                    "waviq_masters.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
