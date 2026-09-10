package com.fpf.blucon.di

import android.content.Context
import com.fpf.blucon.storage.PrefsNames
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val storageModule = module {
    single { androidContext().getSharedPreferences(PrefsNames.APP_PREFS, Context.MODE_PRIVATE) }
}