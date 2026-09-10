package com.fpf.blucon.data

import android.content.Context
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.BluetoothDocsYamlParser

class MetadataRepository(context: Context) {
    private val companyIdMap: Map<Int, String> = BluetoothDocsYamlParser.parseCompanyIdentifiers(context, R.raw.bluetooth_company_id)
    private val serviceUuidMap: Map<Int, String> = BluetoothDocsYamlParser.parseServiceUuids(context, R.raw.bluetooth_service_uuids)

    val companyNames: Set<String>
        get() = companyIdMap.values.toSet()

    val serviceNames: Set<String>
        get() = serviceUuidMap.values.toSet()

    fun getCompanyName(manufacturerId: Int?): String? = manufacturerId?.let{companyIdMap[it]}

    fun getServiceName(serviceId: Int?): String? = serviceId?.let{serviceUuidMap[it]}

    fun findCompanyIds(query: String): List<Int> {
        if (query.isBlank()) return emptyList()
        return companyIdMap
            .filterValues { it.contains(query, ignoreCase = true) }
            .keys
            .toList()
    }
}