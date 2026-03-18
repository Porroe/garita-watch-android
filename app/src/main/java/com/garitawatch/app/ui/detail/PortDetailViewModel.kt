package com.garitawatch.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garitawatch.app.data.local.entity.MonitoredPortEntity
import com.garitawatch.app.data.repository.BorderRepository
import com.garitawatch.app.domain.analytics.AnalyticsEvents
import com.garitawatch.app.domain.analytics.AnalyticsParams
import com.garitawatch.app.domain.analytics.AnalyticsProvider
import com.garitawatch.app.domain.analytics.AnalyticsScreens
import com.garitawatch.app.domain.model.BorderWaitTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortDetailViewModel @Inject constructor(
    private val repository: BorderRepository,
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
}
