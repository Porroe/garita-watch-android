package com.porroe.garitawatch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.porroe.garitawatch.data.local.dao.MonitoredPortDao
import com.porroe.garitawatch.data.local.entity.MonitoredPortEntity

@Database(
    entities = [MonitoredPortEntity::class],
    version = 1,
    exportSchema = false
)
abstract class GaritaWatchDatabase : RoomDatabase() {
    abstract fun monitoredPortDao(): MonitoredPortDao
}
