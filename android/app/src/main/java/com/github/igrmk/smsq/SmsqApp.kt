package com.github.igrmk.smsq

import android.app.Application
import android.content.Context
import com.github.igrmk.smsq.helpers.*
import com.google.crypto.tink.config.TinkConfig
import java.io.File

class SmsqApp : Application() {
    lateinit var log: SimpleFileLogger
    override fun onCreate() {
        TinkConfig.register()
        log = SimpleFileLogger(File(filesDir, Constants.LOG_FILE_NAME), Constants.LOG_HALVING_SIZE)
        super.onCreate()
    }
}

val Context.myApplication: SmsqApp
    get() = applicationContext as SmsqApp

