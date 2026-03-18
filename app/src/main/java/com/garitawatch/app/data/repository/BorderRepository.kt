package com.garitawatch.app.data.repository

import com.garitawatch.app.data.local.dao.MonitoredPortDao
import com.garitawatch.app.data.local.entity.MonitoredPortEntity
import com.garitawatch.app.data.remote.CbpApiService
import com.garitawatch.app.domain.model.BorderWaitTime
import com.garitawatch.app.domain.util.WaitTimeNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BorderRepository @Inject constructor(
    private val apiService: CbpApiService,
    private val monitoredPortDao: MonitoredPortDao
) {
    private val _borderData = MutableStateFlow<List<BorderWaitTime>>(emptyList())
    val borderData: StateFlow<List<BorderWaitTime>> = _borderData.asStateFlow()

    private val _lastUpdatedTime = MutableStateFlow("")
    val lastUpdatedTime: StateFlow<String> = _lastUpdatedTime.asStateFlow()

    private val _lastUpdatedDate = MutableStateFlow("")
    val lastUpdatedDate: StateFlow<String> = _lastUpdatedDate.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    suspend fun refreshData() {
        _isRefreshing.value = true
        try {
            val response = apiService.getBorderWaitTimes()
            val normalized = response.ports?.map { WaitTimeNormalizer.normalize(it) } ?: emptyList()
            
            // Filter out ports where every lane has no usable wait-time data (N/A)
            val filtered = normalized.filter { port ->
                port.passengerLanes.any { it.delayMinutes != null } ||
                port.pedestrianLanes.any { it.delayMinutes != null } ||
                port.commercialLanes.any { it.delayMinutes != null }
            }
            
            _borderData.value = filtered
            _lastUpdatedTime.value = response.lastUpdatedTime ?: ""
            _lastUpdatedDate.value = response.lastUpdatedDate ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle error (e.g., expose error state)
        } finally {
            _isRefreshing.value = false
        }
    }

    fun getMonitoredPorts(): Flow<List<MonitoredPortEntity>> {
        return monitoredPortDao.getAllMonitoredPorts()
    }

    suspend fun addPortToWatchlist(port: MonitoredPortEntity) {
        val maxOrder = monitoredPortDao.getMaxDisplayOrder() ?: -1
        monitoredPortDao.insertPort(port.copy(displayOrder = maxOrder + 1))
    }

    suspend fun removePortFromWatchlist(port: MonitoredPortEntity) {
        monitoredPortDao.deletePort(port)
    }

    suspend fun updatePortOrder(ports: List<MonitoredPortEntity>) {
        val updatedPorts = ports.mapIndexed { index, entity ->
            entity.copy(displayOrder = index)
        }
        monitoredPortDao.updatePorts(updatedPorts)
    }

    fun isPortMonitored(portNumber: String): Flow<Boolean> {
        return monitoredPortDao.isPortMonitored(portNumber)
    }
}
