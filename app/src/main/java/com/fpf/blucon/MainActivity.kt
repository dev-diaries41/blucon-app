package com.fpf.blucon

import android.os.Bundle
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.fpf.blucon.data.ScanDatabase
import com.fpf.blucon.ui.theme.ThemeManager
import com.fpf.blucon.utils.BackupUtils
import com.fpf.blucon.settings.loadSettings
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : ComponentActivity() {
    private val sharedPrefs: SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        var keepSplash = true
        splashScreen.setKeepOnScreenCondition { keepSplash }


        val appSettings = loadSettings(sharedPrefs)
        ThemeManager.updateColorScheme(appSettings.color)
        ThemeManager.updateThemeMode(appSettings.theme)

        updateEdgeToEdge()

        lifecycleScope.launch {
            ThemeManager.themeMode.collectLatest {
                updateEdgeToEdge()
            }
        }

        setContent {
            AppTheme {
                Main(
                    onAppReady = {keepSplash = false},
                    onRestartApp = {restartApp()},
                )
            }
        }
    }

    override fun onResume(){
        super.onResume()
    }
    private fun updateEdgeToEdge() {
        val isDarkTheme = ThemeManager.isDarkTheme(resources)
        enableEdgeToEdge(
            statusBarStyle = if (isDarkTheme) SystemBarStyle.dark(Color.Transparent.toArgb()) else SystemBarStyle.light(Color.Transparent.toArgb(), Color.Black.toArgb()),
            navigationBarStyle = if (isDarkTheme) SystemBarStyle.dark(Color.Transparent.toArgb()) else SystemBarStyle.light(Color.Transparent.toArgb(), Color.Black.toArgb())
        )
    }

    fun restartApp() {
        val cachedDb = BackupUtils.checkCachedDb(application, ScanDatabase.DB_NAME)
        val isRestoreRequired = cachedDb != null
        if (isRestoreRequired) {
            BackupUtils.restoreDbFromCache(application, cachedDb, ScanDatabase.DB_NAME)
        }

        App.resetKoin(application)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        startActivity(intent)
    }
}
