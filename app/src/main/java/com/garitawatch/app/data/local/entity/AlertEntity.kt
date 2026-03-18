package com.garitawatch.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.garitawatch.app.domain.model.LaneType

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val portNumber: String,
    val portName: String,
    val crossingType: String, // Passenger, Pedestrian, Commercial
    val laneTypes: List<LaneType>,
    val thresholdMinutes: Int,
    val durationDays: Int,
    val createdAt: Long,
    val expiresAt: Long
)
