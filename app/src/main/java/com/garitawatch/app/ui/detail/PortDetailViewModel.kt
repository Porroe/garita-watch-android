package com.garitawatch.app.ui.detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garitawatch.app.data.local.entity.AlertEntity
import com.garitawatch.app.data.local.entity.MonitoredPortEntity
import com.garitawatch.app.data.remote.SupabaseManager
import com.garitawatch.app.data.repository.AlertRepository
import com.garitawatch.app.data.repository.BorderRepository
import com.garitawatch.app.domain.analytics.AnalyticsEvents
import com.garitawatch.app.domain.analytics.AnalyticsParams
import com.garitawatch.app.domain.analytics.AnalyticsProvider
import com.garitawatch.app.domain.analytics.AnalyticsScreens
import com.garitawatch.app.domain.model.BorderWaitTime
import com.garitawatch.app.domain.model.LaneType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PortDetailViewModel @Inject constructor(
    private val repository: BorderRepository,
    private val alertRepository: AlertRepository,
    private val supabaseManager: SupabaseManager,
    private val analytics: AnalyticsProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val portNumber: String = checkNotNull(savedStateHandle["portNumber"])

    val port: StateFlow<BorderWaitTime?> = repository.borderData
        .map { ports -> ports.find { it.portNumber == portNumber } }
        .onEach { port ->
            port?.let {
                analytics.trackScreenView(AnalyticsScreens.PORT_DETAIL, "${AnalyticsScreens.PORT_DETAIL}_${it.portName}_${it.crossingName}")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isMonitored: StateFlow<Boolean> = repository.isPortMonitored(portNumber)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _alertCreationStatus = MutableSharedFlow<Result<Unit>>()
    val alertCreationStatus = _alertCreationStatus.asSharedFlow()

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentPort = port.value ?: return@launch
            val entity = MonitoredPortEntity(
                portNumber = currentPort.portNumber,
                portName = currentPort.portName,
                crossingName = currentPort.crossingName,
                border = currentPort.border
            )
            if (isMonitored.value) {
                repository.removePortFromWatchlist(entity)
                analytics.logEvent(
                    AnalyticsEvents.REMOVE_FAVORITE, 
                    mapOf(
                        AnalyticsParams.PORT_NAME to currentPort.portName, 
                        AnalyticsParams.PORT_NUMBER to currentPort.crossingName,
                        AnalyticsParams.SOURCE to "detail"
                    )
                )
            } else {
                repository.addPortToWatchlist(entity)
                analytics.logEvent(
                    AnalyticsEvents.ADD_FAVORITE, 
                    mapOf(
                        AnalyticsParams.PORT_NAME to currentPort.portName, 
                        AnalyticsParams.PORT_NUMBER to currentPort.crossingName,
                        AnalyticsParams.SOURCE to "detail"
                    )
                )
            }
        }
    }

    fun createAlert(crossingType: String, laneTypes: List<LaneType>, threshold: Int, durationDays: Int) {
        viewModelScope.launch {
            val currentPort = port.value ?: return@launch
            
            // Map LaneType to the expected string for Supabase
            // Assuming we take the first selected lane type for the alert creation in Supabase
            // or we might need to create multiple alerts if the backend expects one per lane.
            // Based on the prompt, it seems we might need to handle one lane type at a time or comma separated.
            // Let's create one alert in Supabase for each selected lane type as they are distinct alerts.
            
            val travelMode = crossingType.lowercase()
            
            laneTypes.forEach { laneType ->
                val result = supabaseManager.createWaitTimeAlert(
                    portNumber = currentPort.portNumber,
                    portName = currentPort.portName,
                    crossingName = currentPort.crossingName,
                    travelMode = travelMode,
                    laneType = laneType.name.lowercase(),
                    thresholdMinutes = threshold
                )
                
                if (result.isSuccess) {
                    val now = System.currentTimeMillis()
                    val localAlert = AlertEntity(
                        portNumber = currentPort.portNumber,
                        portName = "${currentPort.portName} - ${currentPort.crossingName}",
                        crossingType = crossingType,
                        laneTypes = listOf(laneType),
                        thresholdMinutes = threshold,
                        durationDays = durationDays,
                        createdAt = now,
                        expiresAt = now + TimeUnit.DAYS.toMillis(durationDays.toLong())
                    )
                    alertRepository.insertAlert(localAlert)
                    _alertCreationStatus.emit(Result.success(Unit))
                } else {
                    Log.e("PortDetailViewModel", "Failed to create alert in Supabase: ${result.exceptionOrNull()}")
                    _alertCreationStatus.emit(Result.failure(result.exceptionOrNull() ?: Exception("Unknown error")))
                }
            }

            analytics.logEvent("create_alert", mapOf(
                "port_name" to currentPort.portName,
                "crossing_type" to crossingType,
                "threshold" to threshold
            ))
        }
    }
}
