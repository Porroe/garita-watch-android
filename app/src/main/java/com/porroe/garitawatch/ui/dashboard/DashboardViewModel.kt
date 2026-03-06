package com.porroe.garitawatch.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.porroe.garitawatch.data.repository.BorderRepository
import com.porroe.garitawatch.domain.model.BorderWaitTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.borderData,
        repository.getMonitoredPorts(),
        repository.isRefreshing
    ) { allData, monitoredEntities, isRefreshing ->
        val monitoredPortNumbers = monitoredEntities.map { it.portNumber }.toSet()
        val monitoredData = allData.filter { it.portNumber in monitoredPortNumbers }
        
        // Use the latest update time from monitored ports
        val lastUpdated = monitoredData.map { it.lastUpdate }
            .filter { it.isNotBlank() }
            .firstOrNull() ?: "Never"

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
}
