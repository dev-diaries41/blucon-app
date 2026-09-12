package com.fpf.blucon.ui.screens.settings

import android.app.Application
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.blucon.data.ScanDatabase
import com.fpf.blucon.data.scans.ScanEntryRepository
import com.fpf.blucon.data.scans.ScanRepository
import com.fpf.blucon.errors.AppException
import com.fpf.blucon.events.BackupEvent
import com.fpf.blucon.events.BackupEventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.fpf.blucon.settings.AppSettings
import com.fpf.blucon.settings.loadSettings
import com.fpf.blucon.settings.saveSettings
import com.fpf.blucon.ui.theme.ColorSchemeType
import com.fpf.blucon.ui.theme.ThemeManager
import com.fpf.blucon.ui.theme.ThemeMode
import com.fpf.blucon.utils.BackupUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SettingsViewModel(
    application: Application,
    private val sharedPrefs: SharedPreferences,
    private val scanEntryRepository: ScanEntryRepository,
    private val scanRepository: ScanRepository,
    ) : AndroidViewModel(application) {
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings

    private val _backupEvent = MutableSharedFlow<BackupEvent>()
    val backupEvent = _backupEvent.asSharedFlow()

    private val _isBackupLoading = MutableStateFlow(false)
    val isBackupLoading: StateFlow<Boolean> = _isBackupLoading

    private val _isRestoreLoading = MutableStateFlow(false)
    val isRestoreLoading: StateFlow<Boolean> = _isRestoreLoading


    companion object {
        private const val TAG = "SettingsViewModel"
    }

    init {
        _appSettings.value = loadSettings(sharedPrefs)
    }

    fun updateTheme(theme: ThemeMode){
        ThemeManager.updateThemeMode(theme)
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(theme = theme)
        saveSettings(sharedPrefs, _appSettings.value)
    }

    fun updateColorScheme(colorScheme: ColorSchemeType){
        ThemeManager.updateColorScheme(colorScheme)
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(color = colorScheme)
        saveSettings(sharedPrefs, _appSettings.value)
    }

    fun backup(uri: Uri){
        _isBackupLoading.value = true
        viewModelScope.launch(Dispatchers.IO){
            try {
                BackupUtils.backup(getApplication(), ScanDatabase.DB_NAME, uri)
                _backupEvent.emit(BackupEvent(BackupEventType.BACKUP, success = true, "Backup successful"))
            }catch (e: AppException.BackupException){
                _backupEvent.emit(BackupEvent(BackupEventType.BACKUP, success = false, e.message))
            }finally {
                _isBackupLoading.emit(false)
            }
        }
    }

    fun exportAsJson(uri: Uri){
        _isBackupLoading.value = true
        viewModelScope.launch(Dispatchers.IO){
            try {
                BackupUtils.exportJson(getApplication(), scanEntryRepository, scanRepository, uri)
                _backupEvent.emit(BackupEvent(BackupEventType.JSON_EXPORT, success = true, "Export successful"))
            }catch (e: AppException.BackupException){
                _backupEvent.emit(BackupEvent(BackupEventType.JSON_EXPORT, success = false, e.message))
            }finally {
                _isBackupLoading.emit(false)
            }
        }
    }

    fun restore(uri: Uri){
        _isRestoreLoading.value = true
        ScanDatabase.close()
        viewModelScope.launch(Dispatchers.IO){
            try {
                BackupUtils.restore(getApplication(), uri)
                _backupEvent.emit(BackupEvent(BackupEventType.RESTORE, success = true, "Restore successful"))
            }
            catch (e: AppException.RestoreException){
                _backupEvent.emit(BackupEvent(BackupEventType.RESTORE, success = false, e.message))
            }
            finally {
                _isRestoreLoading.emit(false)
            }
        }
    }

}
