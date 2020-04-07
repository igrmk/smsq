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

class SimpleFileLogger(
        private val file: File,
        private val halvingSize: Int,
        private val newline: String = System.lineSeparator()) {

    private var writer: BufferedWriter? = null
    private var lock = ReentrantLock()

    private fun prepare() {
        if (file.length() > halvingSize) {
            writer?.close()
            writer = null
            val text = file.readText()
            writer = BufferedWriter(FileWriter(file, true))
            val half = text.indexOf(newline, halvingSize / 2)
            if (half >= 0) {
                writer!!.append(text.substring(half + newline.length)).flush()
            }
        }
        writer = writer ?: BufferedWriter(FileWriter(file, true))
    }

    fun append(text: String): Unit = lock.withLock {
        prepare()
        writer!!.append(text).append(newline)
    }

    fun get(): String = lock.withLock {
        writer?.flush()
        return file.readText()
    }
}
