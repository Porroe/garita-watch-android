package com.porroe.garitawatch.data.repository

import com.porroe.garitawatch.data.local.dao.MonitoredPortDao
import com.porroe.garitawatch.data.local.entity.MonitoredPortEntity
import com.porroe.garitawatch.data.remote.CbpApiService
import com.porroe.garitawatch.domain.model.BorderWaitTime
import com.porroe.garitawatch.domain.util.WaitTimeNormalizer
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
            _borderData.value = normalized
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
        monitoredPortDao.insertPort(port)
    }

    suspend fun removePortFromWatchlist(port: MonitoredPortEntity) {
        monitoredPortDao.deletePort(port)
    }

    fun isPortMonitored(portNumber: String): Flow<Boolean> {
        return monitoredPortDao.isPortMonitored(portNumber)
    }
}
