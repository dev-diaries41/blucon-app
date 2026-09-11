package com.fpf.blucon.ui.screens.search

import com.fpf.blucon.query.SortBy


sealed interface SearchAction {
    data class SetSortBy(val sortBy: SortBy): SearchAction
    data class Search(val query: String): SearchAction

}