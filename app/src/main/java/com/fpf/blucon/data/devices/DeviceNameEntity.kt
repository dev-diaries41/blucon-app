package com.fpf.blucon.data.devices

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "device_name",
    indices = [Index(value = ["name"], unique = true)]
)
data class DeviceNameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)