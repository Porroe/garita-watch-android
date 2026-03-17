package com.porroe.garitawatch.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.porroe.garitawatch.data.local.entity.MonitoredPortEntity
import com.porroe.garitawatch.data.repository.BorderRepository
import com.porroe.garitawatch.domain.model.BorderWaitTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class DashboardUiState(
    val monitoredPorts: List<BorderWaitTime> = emptyList(),
    val isRefreshing: Boolean = false,
    val lastUpdated: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: BorderRepository
) : ViewModel() {

    private val inputDateFormat = SimpleDateFormat("yyyy-M-dd", Locale.US)
    private val outputDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    // Local override for drag-and-drop reordering before persisting
    private val _manualOrder = MutableStateFlow<List<String>?>(null)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.borderData,
        repository.getMonitoredPorts(),
        repository.isRefreshing,
        repository.lastUpdatedTime,
        repository.lastUpdatedDate,
        _manualOrder
    ) { args ->
        val allData = args[0] as List<BorderWaitTime>
        val monitoredEntities = args[1] as List<MonitoredPortEntity>
        val isRefreshing = args[2] as Boolean
        val lastTime = args[3] as String
        val lastDate = args[4] as String
        val manualOrder = args[5] as List<String>?
        
        val sortedEntities = if (manualOrder != null) {
            monitoredEntities.sortedBy { manualOrder.indexOf(it.portNumber) }
        } else {
            monitoredEntities // Already sorted by displayOrder ASC from DAO
        }

        val monitoredPortNumbers = sortedEntities.map { it.portNumber }
        val monitoredDataMap = allData.associateBy { it.portNumber }
        
        // Map data in the order of monitoredEntities
        val monitoredData = monitoredPortNumbers.mapNotNull { monitoredDataMap[it] }
        
        val formattedDate = try {
            if (lastDate.isNotBlank()) {
                val date = inputDateFormat.parse(lastDate)
                if (date != null) outputDateFormat.format(date) else lastDate
            } else lastDate
        } catch (e: Exception) {
            lastDate
        }

        val lastUpdated = if (lastTime.isNotBlank() && formattedDate.isNotBlank()) {
            "$formattedDate $lastTime"
        } else if (monitoredData.isNotEmpty()) {
             monitoredData.firstOrNull()?.lastUpdate ?: "Never"
        } else {
            "Never"
        }

        DashboardUiState(
            monitoredPorts = monitoredData,
            isRefreshing = isRefreshing,
            lastUpdated = lastUpdated
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshData()
        }
    }

    fun movePort(fromIndex: Int, toIndex: Int) {
        val currentList = uiState.value.monitoredPorts.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices) return
        
        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        
        _manualOrder.value = currentList.map { it.portNumber }
        
        // Persist the change
        viewModelScope.launch {
            val entities = repository.getMonitoredPorts().first()
            val entityMap = entities.associateBy { it.portNumber }
            val updatedEntities = currentList.mapNotNull { entityMap[it.portNumber] }
            repository.updatePortOrder(updatedEntities)
            _manualOrder.value = null
        }
    }
}
