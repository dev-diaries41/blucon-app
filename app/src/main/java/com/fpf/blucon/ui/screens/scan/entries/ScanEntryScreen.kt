package com.fpf.blucon.ui.screens.scan.entries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Devices
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.paging.compose.collectAsLazyPagingItems
import com.fpf.blucon.R
import com.fpf.blucon.bluetooth.scan.BTScan
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.action.MenuActionConfig
import com.fpf.blucon.ui.components.bluetooth.CountsList
import com.fpf.blucon.ui.components.bluetooth.ScanEntryStaggeredGrid
import com.fpf.blucon.ui.components.bluetooth.ScanOverviewCard
import com.fpf.blucon.ui.components.common.DropDownMenuWrapper
import com.fpf.blucon.ui.components.modals.BottomSheet
import com.fpf.smartscan.ui.components.common.SlideRevealBox
import com.fpf.smartscan.ui.components.pickers.OptionPicker
import com.fpf.blucon.ui.components.placeholders.EmptyItemsScreen
import com.fpf.blucon.ui.components.search.Header
import com.fpf.blucon.ui.shared.DeviceMetadataViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(FlowPreview::class)
@Composable
fun ScanEntryScreen(
    scan: BTScan?,
    onTopBarChange: (TopBarState) -> Unit,
    onBack: () -> Unit,
    viewModel: ScanEntryViewModel = koinViewModel(),
    deviceMetadataViewModel: DeviceMetadataViewModel = koinViewModel(),
    ) {
    if(scan == null) return

    val state by viewModel.state.collectAsState()
    val devicesState by deviceMetadataViewModel.state.collectAsState()
    val devices = viewModel.devices.collectAsLazyPagingItems()
    val companyCounts = deviceMetadataViewModel.companyCounts.collectAsLazyPagingItems()
    val deviceNameCounts = deviceMetadataViewModel.deviceNameCounts.collectAsLazyPagingItems()

    // actions
    var showMenu by remember { mutableStateOf(false) }
    var showSortOptions by remember { mutableStateOf(false) }
    var showCompanyCounts by remember { mutableStateOf(false) }
    var showDeviceCounts by remember { mutableStateOf(false) }

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
    val screenTitle = stringResource(R.string.title_scan)+ " #${scan.id}"

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

    LaunchedEffect(scan) {
        viewModel.setScan(scan)
        deviceMetadataViewModel.setOverviewInfo(scan)
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
            }
            ScanEntryStaggeredGrid(
                isVisible = devices.itemCount > 0,
                items = devices,
                onOffsetChange = { offset = it },
                maxCollapsePx = maxCollapsablePx,
                headerRow = { Header("${devicesState.totalEntries} devices") },
                overview = {
                    if (devicesState.topManufacturerCounts.isNotEmpty() && devicesState.topDeviceNameCounts.isNotEmpty()) {
                        ScanOverviewCard(
                            topManufacturerCounts = devicesState.topManufacturerCounts,
                            topDeviceNameCounts = devicesState.topDeviceNameCounts,
                            topCollectionCounts = devicesState.topCollectionCounts,
                            onViewAllManufacturers = {showCompanyCounts = true},
                            onViewAllDevices = {showDeviceCounts = true}
                        )
                    }
                }
            )

            EmptyItemsScreen(
                isVisible = devices.itemCount == 0
            )
        }
    }
    OptionPicker(
        isVisible = showSortOptions,
        title = stringResource(R.string.sort),
        options =  viewModel.sortByOptions,
        selectedOption  = state.sortBy,
        onSelect = {
            viewModel.onAction(ScanEntryAction.SetSortBy(it))
            showSortOptions = false
        },
        onClose = {showSortOptions = false}
    )

    BottomSheet(
        show = showCompanyCounts && state.scan != null,
        onDismiss = {showCompanyCounts = false}
    ) {
        CountsList(
            items = companyCounts,
            isVisible = true,
            headerContent = {
                Header(stringResource(R.string.manufacturers), Icons.Filled.Business)
            }
        )
    }

    BottomSheet(
        show = showDeviceCounts && state.scan != null,
        onDismiss = {showDeviceCounts = false}
    ) {
        CountsList(
            items = deviceNameCounts,
            isVisible = true,
            headerContent = {
                Header(stringResource(R.string.devices_names), Icons.Filled.Devices)
            }
        )
    }

}