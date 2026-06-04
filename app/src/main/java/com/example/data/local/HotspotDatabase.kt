package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.HotspotDao
import com.example.data.model.*

@Database(
    entities = [
        RouterEntity::class,
        AccessPackageEntity::class,
        VoucherEntity::class,
        SessionEntity::class,
        TransactionLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class HotspotDatabase : RoomDatabase() {
    abstract fun hotspotDao(): HotspotDao

    companion object {
        @Volatile
        private var INSTANCE: HotspotDatabase? = null

        fun getDatabase(context: Context): HotspotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HotspotDatabase::class.java,
                    "hotspot_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
