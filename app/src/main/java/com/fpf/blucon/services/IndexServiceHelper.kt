package com.fpf.blucon.services


import android.content.Context
import android.content.Intent


fun startIndexing(context: Context) {
    Intent(context.applicationContext, IndexService::class.java)
        .also { intent -> context.applicationContext.startForegroundService(intent) }
}


fun stopIndexing(context: Context){
    context.applicationContext.stopService(Intent(context.applicationContext, IndexService::class.java))
}