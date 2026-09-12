package com.fpf.blucon.ui.components.bluetooth

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.fpf.blucon.ui.components.cards.InfoRow
import com.fpf.blucon.ui.components.search.ListHeader
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CountsList(
    items: LazyPagingItems<Pair<String, Int>>,
    isVisible: Boolean,
    onOffsetChange:( (Int) -> Unit)? = null,
    maxCollapsePx: Int = 0,
    headerLabel: String? = null,
) {
    if (!isVisible) return

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var showScrollToTop by remember { mutableStateOf(false) }
    var totalScrollPx by remember { mutableIntStateOf(0) }
    var initialVisibleItemCount by remember { mutableIntStateOf(0) }

    val connection = remember(maxCollapsePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val deltaPx = -available.y
                totalScrollPx = (totalScrollPx + deltaPx.roundToInt())
                    .coerceIn(0, maxCollapsePx)

                onOffsetChange?.invoke(totalScrollPx)
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(listState, items.itemCount) {
        var previousIndex = 0
        var previousOffset = 0

        snapshotFlow {
            Pair(
                listState.firstVisibleItemIndex,
                listState.firstVisibleItemScrollOffset,
            )
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                Log.d("countlist", "size=${items.itemCount}")
                val visibleItemCount = listState.layoutInfo.visibleItemsInfo.size
                val movedDown = index > previousIndex || (index == previousIndex && offset > previousOffset)
                val movedUp = index < previousIndex || (index == previousIndex && offset < previousOffset)

                if (initialVisibleItemCount == 0 && visibleItemCount > 0) {
                    initialVisibleItemCount = visibleItemCount
                }

                val scrolledPastThreshold = initialVisibleItemCount > 0 && index >= 5 * initialVisibleItemCount

                showScrollToTop = when {
                    index == 0 && offset == 0 -> false
                    movedUp -> false
                    movedDown && scrolledPastThreshold -> true
                    else -> showScrollToTop
                }

                previousIndex = index
                previousOffset = offset
            }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(connection),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            item { headerLabel?.let{ListHeader(it)} }
            items(
                count = items.itemCount,
                key = { index ->
                    val item = items[index]
                    item?.first ?: index
                }
            ) { index ->
                val (key, count) = items[index] ?: return@items
                InfoRow(key, count.toString())
            }
        }

        AnimatedVisibility(
            visible = showScrollToTop,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        showScrollToTop = false
                        onOffsetChange?.invoke(0)
                        listState.scrollToItem(0)
                    }
                },
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                )
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to Top"
                )
            }
        }
    }
}

