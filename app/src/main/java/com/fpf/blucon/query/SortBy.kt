package com.fpf.blucon.query

sealed interface SortBy {
    val descending: Boolean
    data class Date(override val descending: Boolean = true): SortBy
}