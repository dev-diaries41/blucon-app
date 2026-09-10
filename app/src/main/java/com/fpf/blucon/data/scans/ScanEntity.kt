package com.fpf.blucon.data.scans

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "scan",
)
data class ScanEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    )