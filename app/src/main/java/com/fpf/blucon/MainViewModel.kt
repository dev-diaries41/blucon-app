package com.fpf.blucon

import android.app.Application
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fpf.blucon.index.DeviceIndexListener
import com.fpf.blucon.services.startIndexing
import com.fpf.blucon.services.startScanning
import com.fpf.blucon.services.stopIndexing
import com.fpf.blucon.services.stopScanning
import com.fpf.blucon.storage.PrefsKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MainViewModel(
    application: Application,
    private val sharedPrefs: SharedPreferences
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
    }

    val indexProgress = DeviceIndexListener.progress
    val indexStatus = DeviceIndexListener.indexingStatus
    val versionName: String? = try {
        val packageInfo = application.packageManager.getPackageInfo(application.packageName, 0)
        packageInfo.versionName
    } catch (_: Exception) {
        null
    }

    val storedVersion: String?
        get() = sharedPrefs.getString(PrefsKeys.UPDATES, null)

    private val _isUpdatePopUpVisible = MutableStateFlow(storedVersion != versionName && storedVersion != null)
    val  isUpdatePopUpVisible: StateFlow<Boolean> = _isUpdatePopUpVisible

    fun closeUpdatePopUp(){
        _isUpdatePopUpVisible.value = false
        sharedPrefs.edit { putString(PrefsKeys.UPDATES, versionName.toString()) }
    }

    fun setVersion() = sharedPrefs.edit { putString(PrefsKeys.UPDATES, versionName.toString()) }


    fun prepareApp(onAppReady: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if(storedVersion == null) setVersion()
            onAppReady()
        }
    }

    fun startScanService(){
        startScanning(getApplication())
    }

    fun stopScanService(){
        stopScanning(getApplication())
    }

    fun startIndexService(){
        startIndexing(getApplication())
    }

    fun stopIndexService(){
        stopIndexing(getApplication())
    }

    fun onIndexingFinished(){
        DeviceIndexListener.reset()
    }
}