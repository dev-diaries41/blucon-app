package com.fpf.blucon.errors

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class BackupException(override val message: String = "Backup failed", cause: Throwable? = null) : AppException(message, cause)

    class RestoreException(override val message: String = "Restore failed",  cause: Throwable? = null) : AppException(message, cause)

    class SearchException(override val message: String = "An unknown search error occurred", cause: Throwable? = null) : AppException(message, cause)

    class LocationUnavailableException(override val message: String = "Location unavailable", cause: Throwable? = null) : AppException(message, cause)

    class BluetoothUnavailableException(override val message: String = "Bluetooth unavailable", cause: Throwable? = null) : AppException(message, cause)

}