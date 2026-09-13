package com.fpf.blucon.settings

import com.fpf.blucon.ui.theme.ColorSchemeType
import com.fpf.blucon.ui.theme.ThemeMode
import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val color: ColorSchemeType = ColorSchemeType.SMARTSCAN,
)
