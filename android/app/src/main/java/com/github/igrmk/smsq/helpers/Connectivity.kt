package com.github.igrmk.smsq.helpers

import android.content.Context
import android.net.ConnectivityManager

fun Context.checkConnectivity(): Boolean {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo?.isConnected == true
}