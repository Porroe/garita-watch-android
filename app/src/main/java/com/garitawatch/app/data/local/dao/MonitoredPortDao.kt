package com.garitawatch.app.data.local.dao

import androidx.room.*
import com.garitawatch.app.data.local.entity.MonitoredPortEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredPortDao {
    @Query("SELECT * FROM monitored_ports ORDER BY displayOrder ASC")
    fun getAllMonitoredPorts(): Flow<List<MonitoredPortEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPort(port: MonitoredPortEntity)

    @Delete
    suspend fun deletePort(port: MonitoredPortEntity)

    @Update
    suspend fun updatePorts(ports: List<MonitoredPortEntity>)

    @Query("SELECT EXISTS(SELECT 1 FROM monitored_ports WHERE portNumber = :portNumber LIMIT 1)")
    fun isPortMonitored(portNumber: String): Flow<Boolean>

    @Query("SELECT MAX(displayOrder) FROM monitored_ports")
    suspend fun getMaxDisplayOrder(): Int?
}
