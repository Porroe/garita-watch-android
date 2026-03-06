package com.porroe.garitawatch.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.porroe.garitawatch.data.local.entity.MonitoredPortEntity
import com.porroe.garitawatch.data.repository.BorderRepository
import com.porroe.garitawatch.domain.model.BorderWaitTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val allPorts: List<BorderWaitTime> = emptyList(),
    val filteredPorts: List<BorderWaitTime> = emptyList(),
    val monitoredPortNumbers: Set<String> = emptySet(),
    val searchQuery: String = "",
    val showVehicle: Boolean = true,
    val showPedestrian: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: BorderRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _showVehicle = MutableStateFlow(true)
    private val _showPedestrian = MutableStateFlow(true)

    private val filterFlow = combine(
        _searchQuery,
        _showVehicle,
        _showPedestrian
    ) { query, vehicle, pedestrian ->
        FilterState(query, vehicle, pedestrian)
    }

    val uiState: StateFlow<SearchUiState> = combine(
        repository.borderData,
        repository.getMonitoredPorts(),
        filterFlow,
        repository.isRefreshing
    ) { allData, monitoredEntities, filters, isRefreshing ->
        val monitoredNumbers = monitoredEntities.map { it.portNumber }.toSet()
        
        val filtered = allData.filter { port ->
            val matchesQuery = port.portName.contains(filters.query, ignoreCase = true) ||
                               port.crossingName.contains(filters.query, ignoreCase = true)
            
            val hasVehicle = port.passengerLanes.isNotEmpty() || port.commercialLanes.isNotEmpty()
            val hasPedestrian = port.pedestrianLanes.isNotEmpty()
            
            val matchesFilters = (filters.showVehicle && hasVehicle) || (filters.showPedestrian && hasPedestrian)
            
            matchesQuery && matchesFilters
        }

        SearchUiState(
            allPorts = allData,
            filteredPorts = filtered,
            monitoredPortNumbers = monitoredNumbers,
            searchQuery = filters.query,
            showVehicle = filters.showVehicle,
            showPedestrian = filters.showPedestrian,
            isLoading = isRefreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    init {
        // Trigger a refresh if the data is empty
        if (repository.borderData.value.isEmpty()) {
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshData()
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleVehicleFilter() {
        _showVehicle.value = !_showVehicle.value
    }

    fun togglePedestrianFilter() {
        _showPedestrian.value = !_showPedestrian.value
    }

    fun toggleMonitored(port: BorderWaitTime) {
        viewModelScope.launch {
            val isCurrentlyMonitored = uiState.value.monitoredPortNumbers.contains(port.portNumber)
            if (isCurrentlyMonitored) {
                repository.removePortFromWatchlist(
                    MonitoredPortEntity(port.portNumber, port.portName, port.crossingName, port.border)
                )
            } else {
                repository.addPortToWatchlist(
                    MonitoredPortEntity(port.portNumber, port.portName, port.crossingName, port.border)
                )
            }
        }
    }

    private data class FilterState(
        val query: String,
        val showVehicle: Boolean,
        val showPedestrian: Boolean
    )
}
