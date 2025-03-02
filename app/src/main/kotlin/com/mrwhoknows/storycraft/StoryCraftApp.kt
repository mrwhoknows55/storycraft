package com.mrwhoknows.storycraft

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class StoryCraftApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // init koin di
        startKoin {
            androidLogger()
            androidContext(this@StoryCraftApp)
        }
    }
}