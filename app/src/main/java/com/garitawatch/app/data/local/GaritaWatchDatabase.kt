package com.garitawatch.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.garitawatch.app.data.local.dao.AlertDao
import com.garitawatch.app.data.local.dao.MonitoredPortDao
import com.garitawatch.app.data.local.entity.AlertEntity
import com.garitawatch.app.data.local.entity.MonitoredPortEntity

@Database(
    entities = [MonitoredPortEntity::class, AlertEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GaritaWatchDatabase : RoomDatabase() {
    abstract fun monitoredPortDao(): MonitoredPortDao
    abstract fun alertDao(): AlertDao
}
