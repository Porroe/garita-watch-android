package com.garitawatch.app.data.local

import androidx.room.TypeConverter
import com.garitawatch.app.domain.model.LaneType

class Converters {
    @TypeConverter
    fun fromLaneTypeList(value: List<LaneType>): String {
        return value.joinToString(",") { it.name }
    }

    @TypeConverter
    fun toLaneTypeList(value: String): List<LaneType> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").map { LaneType.valueOf(it) }
    }
}
