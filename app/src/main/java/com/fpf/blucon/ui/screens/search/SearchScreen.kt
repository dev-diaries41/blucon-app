package com.fpf.blucon.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.paging.compose.collectAsLazyPagingItems
import com.fpf.blucon.R
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.action.MenuActionConfig
import com.fpf.blucon.ui.components.bluetooth.ScanDevicesList
import com.fpf.blucon.ui.components.common.DropDownMenuWrapper
import com.fpf.blucon.ui.components.common.SearchBar
import com.fpf.smartscan.ui.components.common.SlideRevealBox
import com.fpf.smartscan.ui.components.pickers.OptionPicker
import com.fpf.blucon.ui.components.placeholders.EmptyItemsScreen
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Composable
fun SearchScreen(
    onTopBarChange: (TopBarState) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val searchResultsVisible = searchResults.itemCount > 0

    // actions
    var showMenu by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }

    val menuActions: List<MenuActionConfig> = listOf(
        MenuActionConfig.Button(
            label = stringResource(R.string.sort),
            onClick = { showSortOptions = true },
            enabled = !state.loading,
        ),
    )


    // For dynamic smooth hiding effect of action bars and other components
    var offset by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val maxCollapsablePx = with(density) { 70.dp.toPx() }.toInt()

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                actions = {
                    SearchBar(
                        enabled = true,
                        searchFieldState = viewModel.searchFieldState,
                        onSearch = {viewModel.onAction(SearchAction.Search(viewModel.searchFieldState.text.toString()))},
                        placeholders = listOf("Search devices"),
                        leadingIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            )
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { viewModel.searchFieldState.text }
            .debounce(100.milliseconds)
            .collectLatest { query: CharSequence ->
                viewModel.onAction(SearchAction.Search(query.toString()))
            }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
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
            }
            ScanDevicesList(
                isVisible = searchResultsVisible,
                items = searchResults,
                onOffsetChange = { offset = it },
                maxCollapsePx = maxCollapsablePx,
            )

            EmptyItemsScreen(
                title = if(viewModel.searchFieldState.text.isEmpty()) "Find devices" else  "No results" ,
                isVisible = !searchResultsVisible
            )
        }
    }
    OptionPicker(
        isVisible = showSortOptions,
        title = stringResource(R.string.sort),
        options =  viewModel.sortByOptions,
        selectedOption  = state.sortBy,
        onSelect = {
            viewModel.onAction(SearchAction.SetSortBy(it))
            showSortOptions = false
        },
        onClose = {showSortOptions = false}
    )

}