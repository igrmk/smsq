package com.github.igrmk.smsq.helpers

import android.util.Log

fun lerr(tag: String, msg: String) = Log.e("smsQ/$tag", msg)
fun linf(tag: String, msg: String) = Log.i("smsQ/$tag", msg)
fun ldbg(tag: String, msg: String) = Log.d("smsQ/$tag", msg)
