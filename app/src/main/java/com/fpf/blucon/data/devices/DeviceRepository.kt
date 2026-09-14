package com.fpf.blucon.data.devices


class DeviceRepository(
    private val dao: DeviceDao
) {
    suspend fun insert(deviceNames: List<DeviceEntity>): List<Long> = dao.insert(deviceNames)
    suspend fun delete(ids: List<Long>) = dao.delete(ids)
    suspend fun getByIds(ids: List<Long>): List<DeviceEntity> = dao.getByIds(ids)
    suspend fun getByNames(names: List<String>): List<DeviceEntity> = dao.getByNames(names)
    suspend fun get(): List<DeviceEntity> = dao.get()
    suspend fun getPage(limit: Int, offset: Int): List<DeviceEntity> = dao.getPage(limit=limit, offset=offset)
}