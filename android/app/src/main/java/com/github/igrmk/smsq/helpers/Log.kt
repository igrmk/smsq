package com.github.igrmk.smsq.helpers

import android.content.Context
import android.util.Log
import com.github.igrmk.smsq.BuildConfig
import com.github.igrmk.smsq.myApplication
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private fun date(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.ROOT)
    return sdf.format(Date())
}

fun Context.lerr(tag: String, msg: String) {
    Log.e("smsQ/$tag", msg)
    this.myApplication.log.append("${date()} E/$tag: $msg")
}

fun Context.linf(tag: String, msg: String) {
    Log.i("smsQ/$tag", msg)
    this.myApplication.log.append("${date()} I/$tag: $msg")
}

fun Context.ldbg(tag: String, msg: String) {
    Log.d("smsQ/$tag", msg)
    if (BuildConfig.DEBUG) {
        this.myApplication.log.append("${date()} D/$tag: $msg")
    }
}

