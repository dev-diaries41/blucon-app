package com.fpf.blucon.ui.components.collections

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import com.fpf.blucon.bluetooth.device.DeviceCollection
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.fpf.blucon.ui.components.cards.InfoCard

@Composable
fun DeviceCollectionsList(
    items: List<DeviceCollection>,
    isVisible: Boolean,
    numGridColumns: Int = 2,
    isSelecting: Boolean = false,
    onOffsetChange: (Int) -> Unit,
    isChecked: ((DeviceCollection) -> Boolean)? = null,
    onItemClick: ((DeviceCollection) -> Unit)? = null,
    onItemLongClick: ((DeviceCollection) -> Unit)? = null,
    maxCollapsePx: Int = 0,
    headerRow: (@Composable () -> Unit)? = null,
    overview: (@Composable () -> Unit)? = null,
) {
    if (!isVisible) return

    val scope = rememberCoroutineScope()
    val gridState = rememberLazyStaggeredGridState()

    var showScrollToTop by remember { mutableStateOf(false) }
    var totalScrollPx by remember { mutableIntStateOf(0) }

    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val deltaPx = -available.y
                totalScrollPx = (totalScrollPx + deltaPx.roundToInt()).coerceIn(0, maxCollapsePx)
                onOffsetChange(totalScrollPx)
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(gridState) {
        var previousIndex = 0
        var previousOffset = 0

        snapshotFlow {
            gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->

            val movedDown = index > previousIndex || (index == previousIndex && offset > previousOffset)
            val movedUp = index < previousIndex || (index == previousIndex && offset < previousOffset)

            showScrollToTop = when {
                index == 0 && offset == 0 -> false
                movedUp -> false
                movedDown -> true
                else -> showScrollToTop
            }

            previousIndex = index
            previousOffset = offset
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        LazyVerticalStaggeredGrid (
            columns = StaggeredGridCells.Fixed(numGridColumns),
            state = gridState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(connection),
            contentPadding = PaddingValues(0.dp)
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                overview?.invoke()
            }
            item(span = StaggeredGridItemSpan.FullLine) {
                headerRow?.invoke()
            }

            items(
                count = items.size,
                key = { index ->
                    val item = items[index]
                    item.id
                }
            ) { index ->
                val item = items[index]

                DeviceCollectionCard(
                    collection = item,
                    isSelecting=isSelecting,
                    isChecked = { isChecked?.invoke(item)?: false},
                    onItemClick = onItemClick,
                    onItemLongClick = onItemLongClick
                )
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
                        onOffsetChange(0)
                        gridState.scrollToItem(0)
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

@Composable
fun DeviceCollectionCard(
    collection: DeviceCollection,
    isSelecting: Boolean = false,
    isChecked: ((DeviceCollection) -> Boolean)? = null,
    onItemClick: ((DeviceCollection) -> Unit)? = null,
    onItemLongClick: ((DeviceCollection) -> Unit)? = null,
) {

    InfoCard(
        isSelecting=isSelecting,
        isChecked = { isChecked?.invoke(collection)?: false},
        onClick = { onItemClick?.invoke(collection) },
        onLongClick = { onItemLongClick?.invoke(collection) },
        content = {
            Column(
                modifier = Modifier
                    .widthIn(min = 100.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp)
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${collection.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

