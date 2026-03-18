package com.garitawatch.app.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garitawatch.app.data.local.entity.AlertEntity
import com.garitawatch.app.data.repository.AlertRepository
import com.garitawatch.app.data.repository.BorderRepository
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
class AlertsViewModel @Inject constructor(
    private val alertRepository: AlertRepository,
    private val borderRepository: BorderRepository,
    private val analytics: AnalyticsProvider
) : ViewModel() {

    val alerts: StateFlow<List<AlertEntity>> = alertRepository.allAlerts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val borderData: StateFlow<List<BorderWaitTime>> = borderRepository.borderData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        analytics.trackScreenView(AnalyticsScreens.ALERTS)
    }

    fun deleteAlert(alert: AlertEntity) {
        viewModelScope.launch {
            alertRepository.deleteAlert(alert)
        }
    }

    fun updateAlert(
        alertId: Int,
        crossingType: String,
        laneTypes: List<LaneType>,
        threshold: Int,
        durationDays: Int
    ) {
        viewModelScope.launch {
            val existing = alertRepository.getAlertById(alertId) ?: return@launch
            val now = System.currentTimeMillis()
            val updated = existing.copy(
                crossingType = crossingType,
                laneTypes = laneTypes,
                thresholdMinutes = threshold,
                durationDays = durationDays,
                expiresAt = now + TimeUnit.DAYS.toMillis(durationDays.toLong())
            )
            alertRepository.updateAlert(updated)
        }
    }
}
