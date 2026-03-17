package com.porroe.garitawatch.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.porroe.garitawatch.data.local.entity.MonitoredPortEntity
import com.porroe.garitawatch.data.repository.BorderRepository
import com.porroe.garitawatch.domain.model.BorderWaitTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PortDetailViewModel @Inject constructor(
    private val repository: BorderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val portNumber: String = checkNotNull(savedStateHandle["portNumber"])

    val port: StateFlow<BorderWaitTime?> = repository.borderData
        .map { ports -> ports.find { it.portNumber == portNumber } }
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
            } else {
                repository.addPortToWatchlist(entity)
            }
        }
    }
}
