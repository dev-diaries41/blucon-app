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
import com.fpf.blucon.navigation.Routes
import com.fpf.blucon.ui.screens.donate.DonateScreen
import com.fpf.blucon.ui.screens.scan.ScanScreen
import com.fpf.blucon.ui.screens.scan.ScanViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val scanViewModel: ScanViewModel = viewModel()

    val headerTitle = when (currentRoute) {
        Routes.SCAN -> stringResource(R.string.title_scan)
        Routes.SETTINGS -> stringResource(R.string.title_settings)
        Routes.DONATE -> stringResource(R.string.title_donate)
        else -> ""
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = headerTitle) },
                navigationIcon = {
                    if (currentRoute?.startsWith("settingsDetail") == true || currentRoute?.startsWith("test") == true || currentRoute == "donate" || currentRoute == "scanhistory" || currentRoute == "help") {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
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
                    viewModel=scanViewModel,
                )
            }
            composable(Routes.DONATE){
                DonateScreen()
            }

        }
    }
}