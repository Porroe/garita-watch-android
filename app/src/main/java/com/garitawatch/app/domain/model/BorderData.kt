package com.garitawatch.app.domain.model

data class BorderWaitTime(
    val portNumber: String,
    val portName: String,
    val crossingName: String,
    val border: String,
    val portStatus: String,
    val lastUpdate: String,
    val passengerLanes: List<LaneDetails>,
    val pedestrianLanes: List<LaneDetails>,
    val commercialLanes: List<LaneDetails>
)

data class LaneDetails(
    val type: LaneType,
    val updateTime: String,
    val operationalStatus: String,
    val delayMinutes: Int?,
    val lanesOpen: Int
)

enum class LaneType {
    STANDARD, SENTRI_NEXUS, READY, FAST
}
