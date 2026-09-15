package com.fpf.blucon.data.scans

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "scan_entry",
    primaryKeys = ["scanId", "deviceAddress"],
    indices = [
        Index(value = ["manufacturerId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ScanEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanId"],
            onDelete = ForeignKey.CASCADE

        )
    ]
)
data class ScanEntryEntity (
    val scanId: Long,
    val deviceAddress: String,
    val timestamp: Long,
    val rssi: Int,
    val latitude: Double,
    val longitude: Double,
    val manufacturerId: Int?,
    val deviceName: String? = null
)