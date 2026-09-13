package com.fpf.blucon.query

sealed interface SortBy {
    val descending: Boolean
    data class Date(override val descending: Boolean = true): SortBy
    data class RSSI(override val descending: Boolean = true): SortBy
    data class NAME(override val descending: Boolean = true): SortBy
}