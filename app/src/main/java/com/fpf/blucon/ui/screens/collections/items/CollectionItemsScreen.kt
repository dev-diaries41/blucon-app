package com.fpf.blucon.ui.screens.collections.items


import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DriveFileMoveRtl
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.paging.compose.collectAsLazyPagingItems
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.events.CollectionItemEventType
import com.fpf.blucon.ui.components.common.SelectionHeaderRow
import com.fpf.blucon.ui.action.MenuActionConfig
import com.fpf.blucon.ui.components.common.DropDownMenuWrapper
import com.fpf.blucon.ui.components.common.ActionBar
import com.fpf.blucon.ui.action.ActionConfig
import com.fpf.blucon.ui.components.collections.CollectionItemsList
import com.fpf.blucon.ui.components.placeholders.EmptyItemsScreen
import com.fpf.smartscan.ui.components.common.SlideRevealBox
import com.fpf.smartscan.ui.components.pickers.OptionPicker
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
fun CollectionItemsScreen(
    collection: DeviceCollection?,
    onTopBarChange: (TopBarState) -> Unit,
    onBack: () -> Unit,
    viewModel: CollectionItemsViewModel = koinViewModel(),
    ) {
    if(collection == null) return

    val actionBarHeight = 70

    val context = LocalContext.current

    val state by viewModel.state.collectAsState()
    val items = viewModel.collectionItems.collectAsLazyPagingItems()



    // actions
    var showMenu by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var isMoving by remember { mutableStateOf(false) }
    var isCreatingCollectionAndMoving by remember { mutableStateOf(false) }
    var isAddingTag by remember { mutableStateOf(false) }
    var showMoreActions by remember { mutableStateOf(false) }

    val menuActions: List<MenuActionConfig> = listOf(
        MenuActionConfig.Button(
            label = stringResource(R.string.sort),
            onClick = { showSortOptions = true },
            enabled = !state.loading,
        ),
    )

    val mainActions: List<ActionConfig> = listOf(
        ActionConfig(
            label = stringResource(R.string.add_tag),
            onClick = { isAddingTag = true },
            icon=Icons.Filled.Tag
        ),
        ActionConfig(
            label = stringResource(R.string.move),
            onClick={ isMoving = true },
            enabled = !state.loading,
            icon = Icons.Default.DriveFileMoveRtl
        ),
    )

    // For dynamic smooth hiding effect of action bars and other components
    var offset by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val maxCollapsablePx = with(density) { 70.dp.toPx() }.toInt()
    val screenTitle = collection.name

    LaunchedEffect(collection) {
        viewModel.setCollection(collection)
    }

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = screenTitle,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    Box{
                        IconButton (onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "menu"
                            )
                        }
                        DropDownMenuWrapper(
                            expanded = showMenu,
                            actions = menuActions,
                            onClose = {showMenu = false},
                            modifier = Modifier.widthIn(144.dp)
                        )
                    }
                }
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when(event.type){
                CollectionItemEventType.MOVE -> {
                    event.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
                    if(event.success){
                        items.refresh()
                    }
                }
                CollectionItemEventType.REMOVE -> {
                    event.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show()}
                    if(event.success){
                        items.refresh()
                    }
                }
                else -> {}
            }
        }
    }


    BackHandler(enabled = state.selection.isSelecting) {
        viewModel.onAction(CollectionItemAction.ResetSelection)
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
       Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                SlideRevealBox(
                    isVisible = state.selection.isSelecting,
                    reverse = true,
                    offsetPx = offset,
                    modifier = Modifier
                        .zIndex(1f)
                        .heightIn(max = maxCollapsablePx.dp)
                        .padding(bottom = 8.dp)
                ) {
                    SelectionHeaderRow(
                        selectedCount = state.selection.selectedCount,
                        checked = (state.selection.selectAll && state.selection.excludedItems.isEmpty()) || (state.selection.selectedItems.size == state.totalItems),
                        onSelectAllChange = {
                            viewModel.onAction(
                                CollectionItemAction.SetSelectAll(
                                    it
                                )
                            )
                        }
                    )
                }
           CollectionItemsList(
                    isVisible = items.itemCount > 0,
                    numGridColumns = 2,
                    items = items,
                    isSelecting = state.selection.isSelecting,
                    isChecked = { it in state.selection.selectedItems || (state.selection.selectAll && it !in state.selection.excludedItems)},
                    onItemClick = {
                        if(state.selection.isSelecting){
                            viewModel.onAction(CollectionItemAction.ToggleSelectedMedia(it))
                        }else { }
                    },
                    onItemLongClick = {
                        viewModel.onAction(CollectionItemAction.ToggleSelectionMode)
                        viewModel.onAction(CollectionItemAction.ToggleSelectedMedia(it))
                        offset = 0
                    },
                    onOffsetChange = { offset = it },
                    maxCollapsePx = maxCollapsablePx,
                )

                EmptyItemsScreen(
                    isVisible = items.itemCount == 0
                )
            }

        SlideRevealBox (
            isVisible = state.selection.isSelecting && state.selection.selectedCount > 0,
            offsetPx = offset,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(1f)
                .then(
                    if (offset != 0)
                        Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                    else Modifier
                )
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                ActionBar(
                    actions = mainActions,
                    modifier = Modifier.height(actionBarHeight.dp),
                )
            }
        }
    }


    OptionPicker(
        isVisible = showSortOptions,
        title = stringResource(R.string.sort),
        options =  viewModel.sortByOptions,
        selectedOption  = state.sortBy,
        onSelect = {
            viewModel.onAction(CollectionItemAction.SetSortBy(it))
            showSortOptions = false
        },
        onClose = {showSortOptions = false}
    )
}