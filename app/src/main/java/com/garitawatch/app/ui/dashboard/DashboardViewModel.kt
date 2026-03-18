package com.garitawatch.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garitawatch.app.data.local.entity.MonitoredPortEntity
import com.garitawatch.app.data.repository.BorderRepository
import com.garitawatch.app.data.repository.LocationRepository
import com.garitawatch.app.data.repository.UserPreferencesRepository
import com.garitawatch.app.domain.model.BorderWaitTime
import com.garitawatch.app.domain.util.LocationUtils
import com.garitawatch.app.domain.util.PortLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.text.get

data class DashboardUiState(
    val monitoredPorts: List<BorderWaitTime> = emptyList(),
    val isRefreshing: Boolean = false,
    val isLoading: Boolean = false,
    val lastUpdated: String = "",
    val shouldAskForLocation: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: BorderRepository,
    private val locationRepository: LocationRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val inputDateFormat = SimpleDateFormat("yyyy-M-dd", Locale.US)
    private val outputDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    private val _manualOrder = MutableStateFlow<List<String>?>(null)
    private val _isLoading = MutableStateFlow(true)

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.borderData,
        repository.getMonitoredPorts(),
        repository.isRefreshing,
        repository.lastUpdatedTime,
        repository.lastUpdatedDate,
        userPreferencesRepository.hasAskedLocationPermission,
        _manualOrder,
        _isLoading
    ) { args ->
        val allData = args[0] as List<BorderWaitTime>
        val monitoredEntities = args[1] as List<MonitoredPortEntity>
        val isRefreshing = args[2] as Boolean
        val lastTime = args[3] as String
        val lastDate = args[4] as String
        val hasAskedLocation = args[5] as Boolean
        val manualOrder = args[6] as List<String>?
        val isLoading = args[7] as Boolean
        
        val sortedEntities = if (manualOrder != null) {
            monitoredEntities.sortedBy { manualOrder.indexOf(it.portNumber) }
        } else {
            monitoredEntities
        }

        val monitoredPortNumbers = sortedEntities.map { it.portNumber }
        val monitoredDataMap = allData.associateBy { it.portNumber }
        
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
            isLoading = isLoading,
            lastUpdated = lastUpdated,
            shouldAskForLocation = monitoredData.isEmpty() && !hasAskedLocation && !isLoading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            repository.refreshData()
            _isLoading.value = false
            
            val monitoredPorts = repository.getMonitoredPorts().first()
            if (monitoredPorts.isEmpty()) {
                addNearestPortsIfEmpty()
            }
        }
    }

    fun onLocationPermissionHandled() {
        viewModelScope.launch {
            userPreferencesRepository.setLocationPermissionAsked()
        }
    }

    private fun addNearestPortsIfEmpty() {
        viewModelScope.launch {
            val monitoredPorts = repository.getMonitoredPorts().first()
            if (monitoredPorts.isEmpty()) {
                val location = locationRepository.getCurrentLocation()
                if (location != null) {
                    val allPorts = repository.borderData.value
                    if (allPorts.isNotEmpty()) {
                        val nearestPorts = allPorts.mapNotNull { port ->
                            val coords = PortLocation.getCoordinates(port.portNumber)
                            if (coords != null) {
                                val distance = LocationUtils.calculateDistance(
                                    location.latitude, location.longitude,
                                    coords.latitude, coords.longitude
                                )
                                port to distance
                            } else null
                        }.sortedBy { it.second }
                            .take(3)
                            .map { it.first }

                        nearestPorts.forEach { port ->
                            repository.addPortToWatchlist(
                                MonitoredPortEntity(
                                    portNumber = port.portNumber,
                                    portName = port.portName,
                                    crossingName = port.crossingName,
                                    border = port.border
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshData()
            val monitoredPorts = repository.getMonitoredPorts().first()
            if (monitoredPorts.isEmpty()) {
                addNearestPortsIfEmpty()
            }
        }
    }

    fun movePort(fromIndex: Int, toIndex: Int) {
        val currentList = uiState.value.monitoredPorts.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices) return
        
        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        
        _manualOrder.value = currentList.map { it.portNumber }
        
        viewModelScope.launch {
            val entities = repository.getMonitoredPorts().first()
            val entityMap = entities.associateBy { it.portNumber }
            val updatedEntities = currentList.mapNotNull { entityMap[it.portNumber] }
            repository.updatePortOrder(updatedEntities)
            _manualOrder.value = null
        }
    }
}
