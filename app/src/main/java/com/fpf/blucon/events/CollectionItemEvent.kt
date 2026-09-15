package com.fpf.blucon.events

enum class CollectionItemEventType {
    MOVE,
    REMOVE,
    TAG,
}
data class CollectionItemEvent (
    val type: CollectionItemEventType,
    val success: Boolean,
    val message: String? = null,
    )