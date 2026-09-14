package com.fpf.blucon.services

import android.app.ActivityManager
import android.app.Service
import android.content.Context

fun isServiceRunning(context: Context, serviceClass: Class<out Service>): Boolean {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    @Suppress("DEPRECATION")
    return am.getRunningServices(Int.MAX_VALUE).any {
        it.service.className == serviceClass.name
    }
}