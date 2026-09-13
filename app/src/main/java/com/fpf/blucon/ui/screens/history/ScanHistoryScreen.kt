package com.fpf.blucon.ui.screens.history


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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.paging.compose.collectAsLazyPagingItems
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.events.ScanHistoryEventType
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.action.ActionConfig
import com.fpf.blucon.ui.action.MenuActionConfig
import com.fpf.blucon.ui.components.bluetooth.ScanItemsList
import com.fpf.blucon.ui.components.common.ActionBar
import com.fpf.blucon.ui.components.common.DropDownMenuWrapper
import com.fpf.blucon.ui.components.common.SelectionHeaderRow
import com.fpf.smartscan.ui.components.common.SlideRevealBox
import com.fpf.smartscan.ui.components.pickers.OptionPicker
import com.fpf.blucon.ui.components.placeholders.EmptyItemsScreen
import com.fpf.blucon.ui.components.search.Header
import kotlinx.coroutines.FlowPreview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
fun ScanHistoryScreen(
    onTopBarChange: (TopBarState) -> Unit,
    onViewScan: (BTScan) -> Unit,
    onBack: () -> Unit,
    viewModel: ScanHistoryViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val actionBarHeight = 70
    val state by viewModel.state.collectAsState()

    val scanHistory = viewModel.scanHistory.collectAsLazyPagingItems()

    // actions
    var showMenu by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
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
            label = stringResource(R.string.delete),
            onClick = { viewModel.onAction(ScanHistoryAction.Delete) },
            icon=Icons.Filled.Delete
        ),
    )

    val moreActions: List<MenuActionConfig> = listOf(
        MenuActionConfig.Button(
            label = stringResource(R.string.delete),
            onClick = { viewModel.onAction(ScanHistoryAction.Delete) },
        ),
    )

    // For dynamic smooth hiding effect of action bars and other components
    var offset by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val maxCollapsablePx = with(density) { 70.dp.toPx() }.toInt()
    val screenTitle = stringResource(R.string.title_scan_history)

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
            event.message?.let{ Toast.makeText(context, it, Toast.LENGTH_SHORT).show()

            }
            when(event.type){
                ScanHistoryEventType.DELETE -> {
                    scanHistory.refresh()
                }
            }
        }
    }


    BackHandler(enabled = state.selection.isSelecting) {
        viewModel.onAction(ScanHistoryAction.ResetSelection)
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
                    checked = (state.selection.selectAll && state.selection.excludedItems.isEmpty()) || (state.selection.selectedItems.size == state.totalScans),
                    onSelectAllChange = {
                        viewModel.onAction(
                            ScanHistoryAction.SetSelectAll(
                                it
                            )
                        )
                    }
                )
            }
            ScanItemsList(
                isVisible = scanHistory.itemCount > 0,
                items = scanHistory,
                isSelecting = state.selection.isSelecting,
                isChecked = { it in state.selection.selectedItems || (state.selection.selectAll && it !in state.selection.excludedItems)},
                onItemClick = {
                    if(state.selection.isSelecting){
                        viewModel.onAction(ScanHistoryAction.ToggleSelected(it))
                    }else {
                        onViewScan(it)
                    }
                },
                onLongItemClick = {
                    viewModel.onAction(ScanHistoryAction.ToggleSelectionMode)
                    viewModel.onAction(ScanHistoryAction.ToggleSelected(it))
                    offset = 0
                },
                onOffsetChange = { offset = it },
                maxCollapsePx = maxCollapsablePx,
            ){
                Header("${state.totalScans} scans")
            }

            EmptyItemsScreen(
                isVisible = scanHistory.itemCount == 0
            )
        }


        SlideRevealBox(
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
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    DropDownMenuWrapper(
                        expanded = showMoreActions,
                        actions = moreActions,
                        offset = DpOffset(x = 0.dp, y = -(actionBarHeight).dp),
                        onClose = { showMoreActions = false }
                    )
                }
            }
        }
    }
    OptionPicker(
        isVisible = showSortOptions,
        title = stringResource(R.string.sort),
        options =  viewModel.sortByOptions,
        selectedOption  = state.sortBy,
        onSelect = {
            viewModel.onAction(ScanHistoryAction.SetSortBy(it))
            showSortOptions = false
        },
        onClose = {showSortOptions = false}
    )

}