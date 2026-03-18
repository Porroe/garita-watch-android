package com.garitawatch.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.garitawatch.app.data.local.dao.MonitoredPortDao
import com.garitawatch.app.data.local.entity.MonitoredPortEntity

@Database(
    entities = [MonitoredPortEntity::class],
    version = 2,
    exportSchema = false
)
abstract class GaritaWatchDatabase : RoomDatabase() {
    abstract fun monitoredPortDao(): MonitoredPortDao
}
