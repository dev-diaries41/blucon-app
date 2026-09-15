package com.fpf.blucon.data.devices

import com.fpf.blucon.bluetooth.device.DeviceInfo
import com.fpf.blucon.bluetooth.device.NewDeviceInfo
import com.fpf.blucon.data.mappers.toDomain
import com.fpf.blucon.data.mappers.toEntity


class DeviceRepository(
    private val dao: DeviceDao
) {
    suspend fun insertNew(deviceNames: List<NewDeviceInfo>): List<Long> = dao.insert(deviceNames.map { it.toEntity() })
    suspend fun insert(deviceNames: List<DeviceInfo>): List<Long> = dao.insert(deviceNames.map { it.toEntity() })
    suspend fun delete(ids: List<Long>) = dao.delete(ids)
    suspend fun getByIds(ids: List<Long>): List<DeviceInfo> = dao.getByIds(ids).map{it.toDomain()}
    suspend fun getByNames(names: List<String>): List<DeviceInfo> = dao.getByNames(names).map { it.toDomain() }
    suspend fun get(): List<DeviceInfo> = dao.get().map{it.toDomain()}
    suspend fun getPage(clusterId: Long, limit: Int, offset: Int, descending: Boolean = true): List<DeviceInfo> = if(descending){
        dao.getPageDesc(clusterId, limit=limit, offset=offset)
    }else{
        dao.getPageAsc(clusterId, limit=limit, offset=offset)
    }.map{it.toDomain()}

    suspend fun getUnclusteredItems(): List<Long> = dao.getUnclusteredItemIds()

    suspend fun getByCluster(clusterId: Long): List<DeviceInfo> = dao.getByCluster(clusterId).map { it.toDomain() }
}