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

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.borderData,
        repository.getMonitoredPorts(),
        repository.isRefreshing,
        repository.lastUpdatedTime,
        repository.lastUpdatedDate
    ) { allData, monitoredEntities, isRefreshing, lastTime, lastDate ->
        val monitoredPortNumbers = monitoredEntities.map { it.portNumber }.toSet()
        val monitoredData = allData.filter { it.portNumber in monitoredPortNumbers }
        
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
}
