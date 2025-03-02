package com.mrwhoknows.storycraft

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber

class StoryCraftApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // init timber logs for debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.i("Timber debug logs are enabled")
        }

        // init koin di
        startKoin {
            androidLogger()
            androidContext(this@StoryCraftApp)
        }
    }
}