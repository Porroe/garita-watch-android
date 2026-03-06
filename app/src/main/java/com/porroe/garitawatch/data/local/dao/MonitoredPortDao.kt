package com.porroe.garitawatch.data.local.dao

import androidx.room.*
import com.porroe.garitawatch.data.local.entity.MonitoredPortEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredPortDao {
    @Query("SELECT * FROM monitored_ports")
    fun getAllMonitoredPorts(): Flow<List<MonitoredPortEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPort(port: MonitoredPortEntity)

    @Delete
    suspend fun deletePort(port: MonitoredPortEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM monitored_ports WHERE portNumber = :portNumber LIMIT 1)")
    fun isPortMonitored(portNumber: String): Flow<Boolean>
}
