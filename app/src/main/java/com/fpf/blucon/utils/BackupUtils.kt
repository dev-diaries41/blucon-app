package com.fpf.blucon.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.fpf.blucon.errors.AppException

import java.io.File

object BackupUtils {
    const val BACKUP_FILENAME = "bluecon.zip"
    private const val HASH_FILENAME = "hash.txt"
    private const val TAG = "BackupUtils"

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

    private suspend fun isValidBackupFile(extractedFiles: List<File>): Boolean{
        val hashFile = extractedFiles.find { it.name == HASH_FILENAME }?: return false
        val hashesFromFile: List<String> = hashFile.readLines()
        if(hashesFromFile.isEmpty()) return false

        val otherFiles = extractedFiles.filterNot{it.name == HASH_FILENAME}
        val computedHashes = otherFiles.map{hashFile(it)}
        return hashesFromFile.toSet() == computedHashes.toSet()
    }

}