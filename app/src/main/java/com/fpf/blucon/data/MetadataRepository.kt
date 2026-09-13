package com.fpf.blucon.data

import android.content.Context
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.BluetoothDocsYamlParser
import com.fpf.blucon.bluetooth.PostcodeCsvLoader

class MetadataRepository(context: Context) {
    private val companyIdMap: Map<Int, String> = BluetoothDocsYamlParser.parseCompanyIdentifiers(context, R.raw.bluetooth_company_id)
    private val serviceUuidMap: Map<Int, String> = BluetoothDocsYamlParser.parseServiceUuids(context, R.raw.bluetooth_service_uuids)
    private val postcodeMap: Map<String, Pair<Double, Double>> = PostcodeCsvLoader.load(context.resources, R.raw.se_postcodes)

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

    fun getPostcode(longitude: Double, latitude: Double): String? {
        var nearestPostcode: String? = null
        var minDistance = Double.MAX_VALUE

        for ((postcode, coords) in postcodeMap) {
            val postcodeLatitude = coords.first
            val postcodeLongitude = coords.second

            val dLat = postcodeLatitude - latitude
            val dLon = postcodeLongitude - longitude
            val distance = dLat * dLat + dLon * dLon

            if (distance < minDistance) {
                minDistance = distance
                nearestPostcode = postcode
            }
        }

        return nearestPostcode
    }
}