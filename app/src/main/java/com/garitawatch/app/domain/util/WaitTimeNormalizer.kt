package com.garitawatch.app.domain.util

import com.garitawatch.app.data.remote.model.LaneCategory
import com.garitawatch.app.data.remote.model.LaneInfo
import com.garitawatch.app.data.remote.model.PortResponse
import com.garitawatch.app.domain.model.BorderWaitTime
import com.garitawatch.app.domain.model.LaneDetails
import com.garitawatch.app.domain.model.LaneType

object WaitTimeNormalizer {

    fun normalize(portResponse: PortResponse): BorderWaitTime {
        return BorderWaitTime(
            portNumber = portResponse.portNumber,
            portName = portResponse.portName,
            crossingName = portResponse.crossingName,
            border = portResponse.border,
            portStatus = portResponse.portStatus,
            lastUpdate = "${portResponse.date ?: ""} ${portResponse.time ?: ""}".trim(),
            passengerLanes = normalizeCategory(portResponse.passengerVehicleLanes),
            pedestrianLanes = normalizeCategory(portResponse.pedestrianLanes),
            commercialLanes = normalizeCategory(portResponse.commercialVehicleLanes)
        )
    }

    private fun normalizeCategory(
        category: LaneCategory?
    ): List<LaneDetails> {
        if (category == null) return emptyList()
        val list = mutableListOf<LaneDetails>()

        category.standardLanes?.let {
            list.add(normalizeLane(it, LaneType.STANDARD))
        }
        category.sentriLanes?.let {
            list.add(normalizeLane(it, LaneType.SENTRI_NEXUS))
        }
        category.readyLanes?.let {
            list.add(normalizeLane(it, LaneType.READY))
        }
        category.fastLanes?.let {
            list.add(normalizeLane(it, LaneType.FAST))
        }

        return list
    }

    private fun normalizeLane(laneInfo: LaneInfo, type: LaneType): LaneDetails {
        return LaneDetails(
            type = type,
            updateTime = laneInfo.updateTime ?: "",
            operationalStatus = laneInfo.operationalStatus ?: "",
            delayMinutes = parseWaitTime(laneInfo.delayMinutes),
            lanesOpen = laneInfo.lanesOpen?.toIntOrNull() ?: 0
        )
    }

    /**
     * Normalizes wait time strings like "30 min", "2 hrs 15 min", "N/A" to minutes.
     * Returns null if the value is "N/A", empty, or null.
     */
    fun parseWaitTime(delayStr: String?): Int? {
        if (delayStr == null || delayStr.isBlank() || delayStr.equals("N/A", ignoreCase = true)) {
            return null
        }

        val lowerStr = delayStr.lowercase()
        var totalMinutes = 0

        // Example: "2 hrs 15 min"
        val hourRegex = "(\\d+)\\s*hrs?".toRegex()
        val minRegex = "(\\d+)\\s*min".toRegex()

        val hourMatch = hourRegex.find(lowerStr)
        val minMatch = minRegex.find(lowerStr)

        if (hourMatch != null) {
            totalMinutes += (hourMatch.groupValues[1].toIntOrNull() ?: 0) * 60
        }

        if (minMatch != null) {
            totalMinutes += minMatch.groupValues[1].toIntOrNull() ?: 0
        }

        // If no matches but it's just a number
        if (hourMatch == null && minMatch == null) {
            val digits = lowerStr.filter { it.isDigit() }
            if (digits.isEmpty()) return null
            totalMinutes = digits.toIntOrNull() ?: 0
        }

        return totalMinutes
    }
}
