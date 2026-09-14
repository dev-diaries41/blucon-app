package com.fpf.blucon.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

fun formatDateTime(epochMillis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

fun getTimeInMinutesAndSeconds(milliseconds: Long): Pair<Long, Long> {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    return Pair(minutes, seconds % 60)
}