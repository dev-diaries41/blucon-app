package com.fpf.blucon.data.devices

import com.fpf.blucon.bluetooth.device.DeviceInfo
import com.fpf.blucon.bluetooth.device.NewDeviceInfo
import com.fpf.blucon.data.mappers.toDomain
import com.fpf.blucon.data.mappers.toEntity


class DeviceRepository(
    private val dao: DeviceDao
) {
    suspend fun insert(deviceNames: List<NewDeviceInfo>): List<Long> = dao.insert(deviceNames.map { it.toEntity() })
    suspend fun insert(deviceNames: List<DeviceInfo>): List<Long> = dao.insert(deviceNames.map { it.toEntity() })
    suspend fun delete(ids: List<Long>) = dao.delete(ids)
    suspend fun getByIds(ids: List<Long>): List<DeviceInfo> = dao.getByIds(ids).map{it.toDomain()}
    suspend fun getByNames(names: List<String>): List<DeviceInfo> = dao.getByNames(names).map { it.toDomain() }
    suspend fun get(): List<DeviceInfo> = dao.get().map{it.toDomain()}
    suspend fun getPage(limit: Int, offset: Int): List<DeviceInfo> = dao.getPage(limit=limit, offset=offset).map { it.toDomain() }
}