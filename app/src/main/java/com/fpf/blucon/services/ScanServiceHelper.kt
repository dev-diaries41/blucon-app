package com.fpf.blucon.services

import android.content.Context
import android.content.Intent

fun startScanning(context: Context) {
    Intent(context.applicationContext, ScanService::class.java)
        .also { intent -> context.applicationContext.startForegroundService(intent) }
}


fun stopScanning(context: Context){
    context.applicationContext.stopService(Intent(context.applicationContext, ScanService::class.java))
}