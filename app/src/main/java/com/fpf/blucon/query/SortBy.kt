package com.fpf.blucon.query

sealed interface SortBy {
    val descending: Boolean
    data class Date(override val descending: Boolean = true): SortBy
    data class Rssi(override val descending: Boolean = true): SortBy
    data class Name(override val descending: Boolean = true): SortBy

    data class Size(override val descending: Boolean = true): SortBy
}