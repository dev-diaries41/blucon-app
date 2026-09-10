package com.fpf.blucon.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}