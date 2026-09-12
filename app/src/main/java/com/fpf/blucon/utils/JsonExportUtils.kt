package com.fpf.blucon.utils

import android.content.Context
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.data.scans.ScanRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object JsonExportHelper {
    private const val SCANS_FILE_NAME = "scans.json"
    private const val SCANS_ENTRIES_NAME = "scan_entries.json"
    private const val COMPANY_COUNTS = "company_counts.json"
    private const val DEVICE_NAME_COUNTS = "device_name_counts.json"

    val json = Json {prettyPrint = true}

    suspend fun exportJson(context: Context, scanEntryRepository: ScanEntryRepository, scanRepository: ScanRepository, outputFile: File){
        val scanFile = File(context.cacheDir, SCANS_FILE_NAME)
        val scans = scanRepository.getScans()
        toJson(scans, scanFile)

        val scanEntriesFile = File(context.cacheDir, SCANS_ENTRIES_NAME)
        val scanEntries = scanEntryRepository.getEntries()
        toJson(scanEntries, scanEntriesFile)

        val companyCountsFile = File(context.cacheDir, COMPANY_COUNTS)
        val companyCounts = scanEntryRepository.getManufacturerCounts().toMap()
        toJson(companyCounts, companyCountsFile)

        val deviceNameCountsFile = File(context.cacheDir, DEVICE_NAME_COUNTS)
        val deviceNameCounts = scanEntryRepository.getDeviceNameCounts().toMap()
        toJson(deviceNameCounts, deviceNameCountsFile)

        val filesToZip = listOf(scanFile, scanEntriesFile, companyCountsFile, deviceNameCountsFile)
        zipFiles(outputFile, filesToZip)
    }

    private inline fun <reified T> toJson(items: List<T>, outputFile: File) {
        outputFile.writeText(json.encodeToString(items))
    }
    private fun toJson(items: Map<String, Int>, outputFile: File){
        outputFile.writeText(json.encodeToString(items))
    }
}