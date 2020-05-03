package com.github.igrmk.smsq.helpers

import android.content.Context
import android.util.Log
import com.github.igrmk.smsq.BuildConfig
import com.github.igrmk.smsq.myApplication
import java.text.SimpleDateFormat
import java.util.*

private fun date(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT)
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

interface Logger {
    fun lerr(tag: String, msg: String)
    fun linf(tag: String, msg: String)
    fun ldbg(tag: String, msg: String)
}

class ContextLogger(val context: Context) : Logger {
    override fun lerr(tag: String, msg: String) = context.lerr(tag, msg)
    override fun linf(tag: String, msg: String) = context.linf(tag, msg)
    override fun ldbg(tag: String, msg: String) = context.ldbg(tag, msg)
}

fun Context.logger() = ContextLogger(this)

class NopLogger : Logger {
    override fun lerr(tag: String, msg: String) {}
    override fun linf(tag: String, msg: String) {}
    override fun ldbg(tag: String, msg: String) {}
}
