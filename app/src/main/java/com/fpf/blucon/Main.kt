package com.fpf.blucon

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.navigation.BottomNavigationBar
import com.fpf.blucon.navigation.NavDataKeys
import com.fpf.blucon.navigation.Routes
import com.fpf.blucon.navigation.TopBarState
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

    LaunchedEffect(Unit) {
        mainViewModel.prepareApp { onAppReady() }
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
        NavHost(
            navController = navController,
            startDestination = Routes.HUB,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.HUB) {
                HubScreen(
                    onTopBarChange = { topBarState.value = it },
                    onViewSettings = { navController.navigate(Routes.SETTINGS) },
                    onSearch = { navController.navigate(Routes.SEARCH) },
                    )
            }
            composable(Routes.SCAN) {
                ScanScreen(
                    onTopBarChange = { topBarState.value = it },
                    onViewSettings = { navController.navigate(Routes.SETTINGS) },
                    onViewScanHistory = { navController.navigate(Routes.SCAN_HISTORY) },
                )
            }
            composable(Routes.SCAN_HISTORY) {
                ScanHistoryScreen(
                    onBack = {navController.popBackStack()},
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
                )
            }

            composable(Routes.SEARCH){
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onTopBarChange = { topBarState.value = it },
                )
            }

            composable(Routes.SETTINGS){
                SettingsScreen (
                    onBack = { navController.popBackStack() },
                    onTopBarChange = { topBarState.value = it },
                    onRestartApp = {onRestartApp()},
                )
            }

            composable(Routes.DONATE){
                DonateScreen()
            }
        }
    }
}