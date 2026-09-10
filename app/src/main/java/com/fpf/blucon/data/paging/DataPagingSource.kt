package com.fpf.blucon.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.fpf.blucon.query.SortBy

abstract class DataPagingSource<Output: Any, Filter>(
    private val filter: Filter,
    private val sortBy: SortBy = SortBy.Date(),
) : PagingSource<Int, Output>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Output> {
        val page = params.key ?: 0
        val pageSize = params.loadSize
        val offset = page * pageSize

        // over-fetch by 1 item to detect end of data without using count()
        return try {
            val mediaMetadataList = getItems(filter, sortBy=sortBy, pageSize=pageSize, offset=offset)
            val hasMore = mediaMetadataList.size > pageSize
            val pageItems = if (hasMore) mediaMetadataList.dropLast(1) else mediaMetadataList

            LoadResult.Page(
                data = pageItems,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (hasMore) page + 1 else null
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Output>): Int? {
        return state.anchorPosition?.let { pos ->
            state.closestPageToPosition(pos)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(pos)?.nextKey?.minus(1)
        }
    }

    protected abstract suspend fun getItems(filter: Filter, sortBy: SortBy, pageSize: Int, offset: Int): List<Output>
}