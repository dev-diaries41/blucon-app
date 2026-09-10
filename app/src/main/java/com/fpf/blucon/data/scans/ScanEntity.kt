package com.fpf.blucon.data.scans

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "scan",
)
data class ScanEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val latitude: Float,
    val longitude: Float,
    val timestamp: Long = System.currentTimeMillis(),
    )