package com.fpf.blucon.ui.screens.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpf.blucon.navigation.TopBarState
import com.fpf.blucon.ui.action.SettingActionConfig
import com.fpf.blucon.utils.BackupUtils.BACKUP_FILENAME
import com.fpf.smartscan.ui.components.pickers.OptionPicker
import com.fpf.blucon.R
import com.fpf.blucon.events.BackupEventType
import com.fpf.blucon.ui.components.settings.SettingSection
import com.fpf.blucon.ui.theme.ColorSchemeType
import com.fpf.blucon.ui.theme.ThemeManager
import com.fpf.blucon.ui.theme.ThemeMode
import com.fpf.blucon.ui.theme.format
import com.fpf.blucon.utils.BackupUtils.BACKUP_JSON_FILENAME
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onTopBarChange: (TopBarState) -> Unit,
    onRestartApp: () -> Unit,
    onBack: () -> Unit,
    ) {
    val appSettings by viewModel.appSettings.collectAsState()
    val isBackupLoading by viewModel.isBackupLoading.collectAsState()
    val isRestoreLoading by viewModel.isRestoreLoading.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val versionName: String? = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName
    } catch (_: Exception) {
        null
    }

    val screenTitle = stringResource(R.string.title_settings)

    LaunchedEffect(Unit) {
        onTopBarChange(
            TopBarState(
                title = screenTitle,
                navigationIcon = {
                    IconButton (onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "ArrowBack"
                        )
                    }
                }
            ),
        )
    }

    LaunchedEffect(Unit) {
        viewModel.backupEvent.collect { event ->
            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            when(event.type){
                BackupEventType.RESTORE -> if(event.success) onRestartApp()
                else -> {}
            }
        }
    }

    // Actions
    var isSelectingTheme by remember { mutableStateOf(false) }
    var isSelectingColor by remember { mutableStateOf(false) }

    val generalSettingActions: List<SettingActionConfig> = listOf(
        SettingActionConfig.Button(
            label = stringResource(id = R.string.setting_theme),
            onClick = { isSelectingTheme = true},
            description = ThemeManager.themeModeDisplayNames[appSettings.theme]!!
            ),
        SettingActionConfig.Button(
            label = stringResource(id = R.string.setting_color),
            onClick = { isSelectingColor = true},
            description = ThemeManager.colorSchemeDisplayNames[appSettings.color]!!
        ),
    )

    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { selectedUri ->
            context.contentResolver.takePersistableUriPermission(selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.restore(selectedUri)
        }
    }
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            context.contentResolver.takePersistableUriPermission(
                fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.backup(fileUri)
        }
    }

    val restoreJsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let { selectedUri ->
            context.contentResolver.takePersistableUriPermission(selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            viewModel.restoreJson(selectedUri)
        }
    }


    val exportJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            context.contentResolver.takePersistableUriPermission(
                fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.exportAsJson(fileUri)
        }
    }

    val backupSettingActions: List<SettingActionConfig> = listOf(
        SettingActionConfig.Button(
            enabled = !isBackupLoading && !isRestoreLoading,
            label = stringResource(id = R.string.setting_backup),
            description = stringResource(R.string.setting_backup_restore_description, "Export"),
            onClick = { backupLauncher.launch(BACKUP_FILENAME) },
        ),
        SettingActionConfig.Button(
            enabled = !isBackupLoading && !isRestoreLoading,
            label = stringResource(id = R.string.setting_export),
            description = stringResource(R.string.setting_export_description),
            onClick = { exportJsonLauncher.launch(BACKUP_JSON_FILENAME) },
        ),
        SettingActionConfig.Button(
            enabled = !isBackupLoading && !isRestoreLoading,
            label = stringResource(id = R.string.setting_restore),
            description = stringResource(R.string.setting_backup_restore_description, "Import"),
            onClick = { restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
        ),
        SettingActionConfig.Button(
            enabled = !isBackupLoading && !isRestoreLoading,
            label = stringResource(id = R.string.setting_restore_json),
            description = stringResource(R.string.setting_restore_json_description),
            onClick = { restoreJsonLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
        ),
    )



    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingSection(
                    stringResource(id = R.string.general_settings),
                    settingActionConfigs = generalSettingActions
                )
                SettingSection(
                    stringResource(id = R.string.setting_backup_restore),
                    settingActionConfigs = backupSettingActions
                )
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(132.dp)
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                versionName?.let {
                    Text(
                        text = "Version $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = stringResource(R.string.copyright),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    OptionPicker(
        isVisible = isSelectingTheme,
        title = stringResource(id = R.string.setting_theme),
        selectedOption = appSettings.theme,
        options = ThemeMode.entries.map{it.format() to it},
        onClose = { isSelectingTheme = false },
        onSelect = {
            viewModel.updateTheme(it)
            isSelectingTheme = false
        },
    )

    OptionPicker(
        isVisible = isSelectingColor,
        title = stringResource(id = R.string.setting_color),
        selectedOption = appSettings.color,
        options = ColorSchemeType.entries.map{it.format() to it},
        onClose = { isSelectingColor = false },
        onSelect = {
            viewModel.updateColorScheme(it)
            isSelectingColor = false
        },
    )
}