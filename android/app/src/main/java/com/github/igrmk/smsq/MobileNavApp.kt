package com.github.igrmk.smsq

import android.app.Activity
import android.app.Application
import com.github.igrmk.smsq.helpers.*

class SmsqApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

val Activity.myApplication: SmsqApp
    get() = application as SmsqApp

