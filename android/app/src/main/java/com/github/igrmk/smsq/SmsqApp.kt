package com.github.igrmk.smsq

import android.app.Application
import android.content.Context
import com.github.igrmk.dull.FileLogger
import com.google.crypto.tink.config.TinkConfig
import java.io.File

class SmsqApp : Application() {
    lateinit var log: FileLogger
    override fun onCreate() {
        TinkConfig.register()
        log = FileLogger(File(filesDir, Constants.LOG_FILE_NAME), Constants.LOG_HALVING_SIZE)
        super.onCreate()
    }
}

val Context.myApplication: SmsqApp
    get() = applicationContext as SmsqApp

