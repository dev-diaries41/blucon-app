package com.fpf.blucon.data.devices


class DeviceNameRepository(
    private val dao: DeviceNameDao
) {
    suspend fun insert(deviceNames: List<DeviceNameEntity>): List<Long> = dao.insert(deviceNames)
    suspend fun delete(ids: List<Long>) = dao.delete(ids)
    suspend fun getByIds(ids: List<Long>): List<DeviceNameEntity> = dao.getByIds(ids)
    suspend fun getByNames(names: List<String>): List<DeviceNameEntity> = dao.getByNames(names)
    suspend fun get(): List<DeviceNameEntity> = dao.get()
    suspend fun getPage(limit: Int, offset: Int): List<DeviceNameEntity> = dao.getPage(limit=limit, offset=offset)
}