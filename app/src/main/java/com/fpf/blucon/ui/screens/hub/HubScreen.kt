package com.fpf.blucon.ui.screens.hub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.components.bluetooth.CountsList
import com.fpf.blucon.ui.components.bluetooth.DeviceOverviewCard
import com.fpf.blucon.ui.components.modals.BottomSheet
import com.fpf.blucon.ui.components.placeholders.EmptyItemsScreen
import com.fpf.blucon.ui.components.search.Header
import com.fpf.blucon.ui.shared.DeviceMetadataViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
fun HubScreen(
    onTopBarChange: (TopBarState) -> Unit,
    onViewSettings: () -> Unit,
    onSearch: () -> Unit,
    onViewCollection: (DeviceCollection) -> Unit,
    onViewAllCollections: () -> Unit,
    viewModel: DeviceMetadataViewModel = koinViewModel(),
    ) {
    val screenTitle = stringResource(R.string.title_hub)
    val state by viewModel.state.collectAsState()
    val companyCounts = viewModel.companyCounts.collectAsLazyPagingItems()
    val deviceNameCounts = viewModel.deviceNameCounts.collectAsLazyPagingItems()
    val showEmptyScreen = companyCounts.itemCount == 0 && deviceNameCounts.itemCount == 0
    var showDevicesCounts by remember { mutableStateOf(false) }
    var showCompanyCounts by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = screenTitle,
                actions = {
                    IconButton(onClick = onSearch) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                    IconButton(onClick = onViewSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        )
    }

    LaunchedEffect(Unit) {
        viewModel.setOverviewInfo()
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
            if (state.topManufacturerCounts.isNotEmpty() && state.topDeviceNameCounts.isNotEmpty()) {
                DeviceOverviewCard(
                    totalEntries = state.totalEntries,
                    topManufacturerCounts = state.topManufacturerCounts,
                    topDeviceNameCounts = state.topDeviceNameCounts,
                    topCollectionCounts = state.topCollectionCounts,
                    onViewAllManufacturers = {showCompanyCounts = true},
                    onViewAllDevices = {showDevicesCounts = true},
                    onCollectionClick = { onViewCollection(it) },
                    onViewCollections = {onViewAllCollections()}
                )
            }
        }

        EmptyItemsScreen(
            icon = {
                Icon(
                    imageVector = Icons.Filled.Hub,
                    contentDescription = "Hub icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(96.dp)
                )
            },
            title = "No data available" ,
            description = "Data will be updated after scans",
            isVisible = showEmptyScreen
        )
    }

    BottomSheet(
        show = showDevicesCounts,
        onDismiss = {showDevicesCounts = false}
    ) {
        CountsList(
            items = deviceNameCounts,
            isVisible = true,
            headerContent = {
                Header(stringResource(R.string.devices_names), Icons.Filled.Devices)
            },
        )
    }

    BottomSheet(
        show = showCompanyCounts,
        onDismiss = {showCompanyCounts = false}
    ) {
        CountsList(
            items = companyCounts,
            isVisible = true,
            headerContent = {
                Header(stringResource(R.string.manufacturers), Icons.Filled.Business)
            },
        )
    }

}