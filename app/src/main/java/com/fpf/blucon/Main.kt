package com.fpf.blucon

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.navigation.NavDataKeys
import com.fpf.blucon.navigation.Routes
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.screens.devices.DevicesScreen
import com.fpf.blucon.ui.screens.donate.DonateScreen
import com.fpf.blucon.ui.screens.history.ScanHistoryScreen
import com.fpf.blucon.ui.screens.scan.ScanScreen
import com.fpf.blucon.ui.screens.search.SearchScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    val navController = rememberNavController()
    val topBarState = remember { mutableStateOf(TopBarState()) }

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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.SCAN,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.SCAN) {
                ScanScreen(
                    onTopBarChange = { topBarState.value = it },
                    onViewScanHistory = { navController.navigate(Routes.SCAN_HISTORY) },
                    onSearch = {navController.navigate(Routes.SEARCH)}
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

                DevicesScreen(
                    onTopBarChange = { topBarState.value = it },
                    scan = scan,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.SEARCH){
                SearchScreen(
                    onTopBarChange = { topBarState.value = it },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.DONATE){
                DonateScreen()
            }

        }
    }
}