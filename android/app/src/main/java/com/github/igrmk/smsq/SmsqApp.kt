package com.github.igrmk.smsq

import android.app.Activity
import android.app.Application
import com.github.igrmk.smsq.helpers.*
import com.google.crypto.tink.config.TinkConfig

class SmsqApp : Application() {
    override fun onCreate() {
        TinkConfig.register()
        super.onCreate()
    }
}

val Activity.myApplication: SmsqApp
    get() = application as SmsqApp

