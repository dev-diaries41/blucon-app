package com.fpf.blucon.events

enum class ScanHistoryEventType {
    DELETE,
}
data class ScanHistoryEvent (
    val type: ScanHistoryEventType,
    val success: Boolean,
    val message: String? = null
)