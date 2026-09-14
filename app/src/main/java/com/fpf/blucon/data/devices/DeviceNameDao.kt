package com.fpf.blucon.data.devices

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeviceNameDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(deviceNames: List<DeviceNameEntity>): List<Long>

    @Query("DELETE FROM device_name WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("SELECT * FROM device_name WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<DeviceNameEntity>

    @Query("SELECT * FROM device_name WHERE name IN (:names)")
    suspend fun getByNames(names: List<String>): List<DeviceNameEntity>

    @Query("SELECT * FROM device_name")
    suspend fun get(): List<DeviceNameEntity>

    @Query("SELECT * FROM device_name ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getPage(limit: Int, offset: Int): List<DeviceNameEntity>
}