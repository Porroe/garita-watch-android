package com.porroe.garitawatch.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monitored_ports")
data class MonitoredPortEntity(
    @PrimaryKey
    val portNumber: String,
    val portName: String,
    val crossingName: String,
    val border: String,
    val displayOrder: Int = 0
)
