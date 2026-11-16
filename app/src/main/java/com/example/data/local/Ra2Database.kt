package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [GameFileEntity::class, ModPackageEntity::class, LaunchLogEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class Ra2Database : RoomDatabase() {
    abstract fun ra2Dao(): Ra2Dao

    companion object {
        @Volatile
        private var INSTANCE: Ra2Database? = null

        fun getInstance(context: Context): Ra2Database {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    Ra2Database::class.java,
                    "ra2_arm64_launcher.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
