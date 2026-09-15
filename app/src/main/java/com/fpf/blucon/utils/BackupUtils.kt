package com.fpf.blucon.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.fpf.blucon.bluetooth.scan.BTScan
import com.fpf.blucon.bluetooth.scan.BTScanEntry
import com.fpf.blucon.errors.AppException
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.data.scans.ScanRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

object BackupUtils {
    const val BACKUP_FILENAME = "bluecon.zip"
    private const val HASH_FILENAME = "hash.txt"

    const val BACKUP_JSON_FILENAME = "bluecon_json.zip"

    private const val HASH_JSON_FILENAME = "hash_json.txt"
    private const val TAG = "BackupUtils"


    private const val SCANS_FILE_NAME = "scans.json"
    private const val SCANS_ENTRIES_NAME = "scan_entries.json"
    private const val COMPANY_COUNTS = "company_counts.json"
    private const val DEVICE_NAME_COUNTS = "device_name_counts.json"

    private val json = Json {prettyPrint = true}

    suspend fun backup(context: Context, dbName: String, outputUri: Uri){
        val indexZipFile = File(context.cacheDir, BACKUP_FILENAME)
        val hashFile = File(context.cacheDir, HASH_FILENAME)
        val dbPath = context.getDatabasePath(dbName)
        val filesToZip = listOf( hashFile, dbPath)

        try {
            val hashes: List<String> = filesToZip.filter { it.exists() && it != hashFile }.map{hashFile(it)}
            hashFile.writeText(hashes.joinToString("\n") )
            zipFiles(indexZipFile, filesToZip)
            copyToUri(context, outputUri, indexZipFile)
        }catch (e: Exception){
            Log.e(TAG, "Unknow backup error", e)
            throw AppException.BackupException(cause = e)
        }
        finally {
            indexZipFile.delete()
            hashFile.delete()
        }

    }

    // Ensure DB is closed beforehand
    suspend fun restore(context: Context, uri: Uri){
        val indexZipFile = File(context.cacheDir, BACKUP_FILENAME)
        try {
            copyFromUri(context, uri, indexZipFile)
            val extractedFiles = unzipFiles(indexZipFile, context.filesDir)

            if(!isValidBackupFile(extractedFiles)){
                extractedFiles.forEach { it.delete() }
                throw AppException.RestoreException("Invalid backup file")
            }
        }
        catch (e: Exception){
            Log.e(TAG, "Unknow restore error", e)
            throw AppException.RestoreException(cause = e)
        }
        finally {
            indexZipFile.delete()
        }

    }

    suspend fun exportJson(context: Context, scanEntryRepository: ScanEntryRepository, scanRepository: ScanRepository, outputUri: Uri){
        try{
            val hashFile = File(context.cacheDir, HASH_JSON_FILENAME)
            val scanFile = File(context.cacheDir, SCANS_FILE_NAME)
            val scans = scanRepository.getScans()
            toJson(scans, scanFile)

            val scanEntriesFile = File(context.cacheDir, SCANS_ENTRIES_NAME)
            val scanEntries = scanEntryRepository.getEntries()
            toJson(scanEntries, scanEntriesFile)

            val companyCountsFile = File(context.cacheDir, COMPANY_COUNTS)
            val companyCounts = scanEntryRepository.getManufacturerCounts().associate { it.first to it.third }
            toJson(companyCounts, companyCountsFile)

            val deviceNameCountsFile = File(context.cacheDir, DEVICE_NAME_COUNTS)
            val deviceNameCounts = scanEntryRepository.getDeviceNameCounts().associate { it.first to it.third }
            toJson(deviceNameCounts, deviceNameCountsFile)


            val filesToZip = listOf(scanFile, scanEntriesFile, companyCountsFile, deviceNameCountsFile, hashFile)
            val hashes: List<String> = filesToZip.filter { it.exists() && it != hashFile }.map{hashFile(it)}
            hashFile.writeText(hashes.joinToString("\n") )

            val zipFile = File(context.cacheDir, BACKUP_JSON_FILENAME)
            zipFiles(zipFile, filesToZip)
            copyToUri(context, outputUri, zipFile)
        }catch (e: Exception){
            Log.e(TAG, "Unknow export error", e)
            throw AppException.BackupException("export failed", cause = e)
        }
    }


    suspend fun restoreJson(context: Context, uri: Uri, scanEntryRepository: ScanEntryRepository, scanRepository: ScanRepository){
        val backupZipFile = File(context.cacheDir, BACKUP_JSON_FILENAME)
        try {
            copyFromUri(context, uri, backupZipFile)
            val extractedFiles = unzipFiles(backupZipFile, context.cacheDir)

            if(!isValidBackupFile(extractedFiles, isJson = true)){
                extractedFiles.forEach { it.delete() }
                throw AppException.RestoreException("Invalid backup file")
            }

            val files = getFiles(context)
            val scans = parseJsonFiles<BTScan>(files[SCANS_FILE_NAME]!!)
            val scanEntries = parseJsonFiles<BTScanEntry>(files[SCANS_ENTRIES_NAME]!!)
            scanRepository.insertScanWithId(scans)
            scanEntryRepository.addEntries(scanEntries)
        }
        catch (e: Exception){
            Log.e(TAG, "Unknow restore error", e)
            throw AppException.RestoreException(cause = e)
        }
        finally {
            backupZipFile.delete()
        }

    }

    private fun getFiles(context: Context): Map<String, File>{
        return mapOf(
            SCANS_FILE_NAME to File(context.cacheDir, SCANS_FILE_NAME),
            SCANS_ENTRIES_NAME to File(context.cacheDir, SCANS_ENTRIES_NAME),
            COMPANY_COUNTS to File(context.cacheDir, COMPANY_COUNTS),
            DEVICE_NAME_COUNTS to  File(context.cacheDir, DEVICE_NAME_COUNTS),
        )
    }

    private inline fun <reified T>parseJsonFiles(file: File): List<T>{
        val jsonStr = file.bufferedReader().readText()
        return Json.decodeFromString<List<T>>(jsonStr)
    }

    private fun parseJsonFiles(file: File): Map<String, Int>{
        val jsonStr = file.bufferedReader().readText()
        return Json.decodeFromString<Map<String, Int>>(jsonStr)
    }
    private inline fun <reified T> toJson(items: List<T>, outputFile: File) {
        outputFile.writeText(json.encodeToString(items))
    }
    private fun toJson(items: Map<String, Int>, outputFile: File){
        outputFile.writeText(json.encodeToString(items))
    }

    fun checkCachedDb(context: Context, dbName: String): File?{
        val cachedDB = File(context.filesDir, dbName)
        return if(cachedDB.exists()) cachedDB else null
    }

    fun restoreDbFromCache(context: Context, cachedDbFile: File,  dbName: String){
        if(!cachedDbFile.exists()) return
        val dbPath = context.getDatabasePath(dbName)
        Log.d(TAG, "Database cache found, restoring...")
        cachedDbFile.copyTo(dbPath, overwrite = true)
        cachedDbFile.delete()
    }


    private suspend fun isValidBackupFile(extractedFiles: List<File>, isJson: Boolean = false): Boolean{
        val hashFileName = if(isJson) HASH_JSON_FILENAME else HASH_FILENAME
        val hashFile = extractedFiles.find { it.name == hashFileName }?: return false
        val hashesFromFile: List<String> = hashFile.readLines()
        if(hashesFromFile.isEmpty()) return false

        val otherFiles = extractedFiles.filterNot{it.name == hashFileName}
        val computedHashes = otherFiles.map{hashFile(it)}
        return hashesFromFile.toSet() == computedHashes.toSet()
    }

}