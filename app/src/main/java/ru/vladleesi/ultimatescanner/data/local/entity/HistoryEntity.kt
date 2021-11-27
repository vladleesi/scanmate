package ru.vladleesi.ultimatescanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_entity")
data class HistoryEntity(val type: String, val value: String, val date: String) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
