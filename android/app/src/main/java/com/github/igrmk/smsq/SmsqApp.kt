package com.github.igrmk.smsq

import android.app.Application
import android.content.Context
import com.github.igrmk.dull.FileLogger
import com.github.igrmk.smsq.helpers.linf
import com.github.igrmk.smsq.helpers.myPreferences
import com.github.igrmk.smsq.helpers.retired
import com.github.igrmk.smsq.helpers.versionCode
import com.google.crypto.tink.config.TinkConfig
import java.io.File

class SmsqApp : Application() {
    private val tag = this::class.simpleName!!
    lateinit var log: FileLogger
    override fun onCreate() {
        TinkConfig.register()
        log = FileLogger(File(filesDir, Constants.LOG_FILE_NAME), Constants.LOG_HALVING_SIZE)
        linf(tag, "starting version code ${BuildConfig.VERSION_CODE}...")
        if (BuildConfig.VERSION_CODE > myPreferences.versionCode) {
            myPreferences.retired = false
            myPreferences.versionCode = BuildConfig.VERSION_CODE
        }
        super.onCreate()
    }
}

val Context.myApplication: SmsqApp
    get() = applicationContext as SmsqApp

