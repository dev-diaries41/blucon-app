package com.fpf.blucon

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.*
import com.fpf.blucon.bluetooth.device.DeviceCollection
import com.fpf.blucon.bluetooth.scan.BTScan
import com.fpf.blucon.index.IndexingStatus
import com.fpf.blucon.navigation.BottomNavigationBar
import com.fpf.blucon.navigation.NavDataKeys
import com.fpf.blucon.navigation.Routes
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.components.common.ProgressBar
import com.fpf.blucon.ui.screens.collections.CollectionsScreen
import com.fpf.blucon.ui.screens.collections.items.CollectionItemsScreen
import com.fpf.blucon.ui.screens.scan.entries.ScanEntryScreen
import com.fpf.blucon.ui.screens.donate.DonateScreen
import com.fpf.blucon.ui.screens.history.ScanHistoryScreen
import com.fpf.blucon.ui.screens.hub.HubScreen
import com.fpf.blucon.ui.screens.scan.ScanScreen
import com.fpf.blucon.ui.screens.search.SearchScreen
import com.fpf.blucon.ui.screens.settings.SettingsScreen
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
    onAppReady: () -> Unit,
    onRestartApp: () -> Unit,
) {
    val navController = rememberNavController()
    val topBarState = remember { mutableStateOf(TopBarState()) }
    val mainViewModel: MainViewModel = koinViewModel()
    val indexProgress by mainViewModel.indexProgress.collectAsState()
    val indexStatus by mainViewModel.indexStatus.collectAsState()
    val isIndexing =  indexStatus == IndexingStatus.ACTIVE

    LaunchedEffect(Unit) {
        mainViewModel.prepareApp { onAppReady() }
    }

    LaunchedEffect(indexStatus) {
        when(indexStatus){
            IndexingStatus.COMPLETE,IndexingStatus.FAILED, IndexingStatus.CANCELLED  -> {
                mainViewModel.onIndexingFinished()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(topBarState.value.title)
                },
                navigationIcon = {
                    topBarState.value.navigationIcon?.invoke()
                },
                actions = {
                    topBarState.value.actions?.invoke(this)
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.padding(paddingValues)
        ) {
            ProgressBar(
                label = "Indexing devices ${"%.0f".format(indexProgress * 100)}%",
                isVisible = isIndexing,
                progress = 0f,
                modifier = Modifier.zIndex(10F).padding(bottom=16.dp, start = 16.dp, end=16.dp)
            )
            NavHost(
                navController = navController,
                startDestination = Routes.HUB,
            ) {
                composable(Routes.HUB) {
                    HubScreen(
                        onTopBarChange = { topBarState.value = it },
                        onViewSettings = { navController.navigate(Routes.SETTINGS) },
                        onSearch = { navController.navigate(Routes.SEARCH) },
                        onViewAllCollections = { navController.navigate(Routes.COLLECTIONS) },
                        onViewCollection = { collection -> navController.currentBackStackEntry?.savedStateHandle?.set(NavDataKeys.COLLECTION, collection)
                            navController.navigate(Routes.COLLECTION_ITEMS)
                        },
                    )
                }
                composable(Routes.SCAN) {
                    ScanScreen(
                        onScan = { mainViewModel.startScanService() },
                        onStopScan = { mainViewModel.stopScanService() },
                        onTopBarChange = { topBarState.value = it },
                        onViewSettings = { navController.navigate(Routes.SETTINGS) },
                        onViewScanHistory = { navController.navigate(Routes.SCAN_HISTORY) },
                        onIndex = {
                            if(isIndexing) return@ScanScreen
                            mainViewModel.startIndexService(

                            )}
                    )
                }
                composable(Routes.SCAN_HISTORY) {
                    ScanHistoryScreen(
                        onBack = { navController.popBackStack() },
                        onViewScan = { scan ->
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set(NavDataKeys.SCAN, scan)

                            navController.navigate(Routes.SCAN_DEVICES)
                        },
                        onTopBarChange = { topBarState.value = it },
                    )
                }
                composable(
                    route = Routes.SCAN_DEVICES,
                ) { _ ->
                    val scan =
                        navController.previousBackStackEntry?.savedStateHandle?.get<BTScan>(
                            NavDataKeys.SCAN
                        )

                    ScanEntryScreen(
                        onTopBarChange = { topBarState.value = it },
                        scan = scan,
                        onBack = { navController.popBackStack() },
                        onViewAllCollections = { navController.navigate(Routes.COLLECTIONS) },
                        onViewCollection = { collection -> navController.currentBackStackEntry?.savedStateHandle?.set(NavDataKeys.COLLECTION, collection)
                            navController.navigate(Routes.COLLECTION_ITEMS)
                        },
                    )
                }

                composable(Routes.COLLECTIONS) {
                    CollectionsScreen(
                        onBack = { navController.popBackStack() },
                        onTopBarChange = { topBarState.value = it },
                        onViewCollection = { collection ->
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set(NavDataKeys.COLLECTION, collection)

                            navController.navigate(Routes.COLLECTION_ITEMS)
                        },
                    )
                }
                composable(
                    route = Routes.COLLECTION_ITEMS,
                ) { _ ->
                    val collection =
                        navController.previousBackStackEntry?.savedStateHandle?.get<DeviceCollection>(
                            NavDataKeys.COLLECTION
                        )

                    CollectionItemsScreen(
                        onTopBarChange = { topBarState.value = it },
                        collection = collection,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.SEARCH) {
                    SearchScreen(
                        onBack = { navController.popBackStack() },
                        onTopBarChange = { topBarState.value = it },
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onTopBarChange = { topBarState.value = it },
                        onRestartApp = { onRestartApp() },
                    )
                }

                composable(Routes.DONATE) {
                    DonateScreen()
                }
            }
        }
    }
}