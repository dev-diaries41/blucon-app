package com.fpf.blucon

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.fpf.blucon.bluetooth.BTScan
import com.fpf.blucon.navigation.NavDataKeys
import com.fpf.blucon.navigation.Routes
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.screens.devices.DevicesScreen
import com.fpf.blucon.ui.screens.donate.DonateScreen
import com.fpf.blucon.ui.screens.history.ScanHistoryScreen
import com.fpf.blucon.ui.screens.scan.ScanScreen
import com.fpf.blucon.ui.screens.scan.ScanViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    val navController = rememberNavController()
    val topBarState = remember { mutableStateOf(TopBarState()) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val headerTitle = when (currentRoute) {
        Routes.SCAN -> stringResource(R.string.title_scan)
        Routes.SETTINGS -> stringResource(R.string.title_settings)
        Routes.DONATE -> stringResource(R.string.title_donate)
        else -> ""
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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Routes.SCAN,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Routes.SCAN) {
                ScanScreen()
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
            composable(Routes.DONATE){
                DonateScreen()
            }

        }
    }
}