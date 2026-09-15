package com.fpf.blucon.events

enum class BackupEventType {
    RESTORE_JSON,

    RESTORE,
    BACKUP,
    JSON_EXPORT
}
data class BackupEvent (
    val type: BackupEventType,
    val success: Boolean,
    val message: String? = null
)