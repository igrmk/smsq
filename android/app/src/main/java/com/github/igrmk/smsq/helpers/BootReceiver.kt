package com.github.igrmk.smsq.helpers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.github.igrmk.smsq.services.ResenderService

class BootReceiver : BroadcastReceiver() {
    private val tag: String = this::class.simpleName!!

    override fun onReceive(context: Context, intent: Intent) {
        if (!context.myPreferences.on) {
            return
        }
        ldbg(tag, "action received: " + intent.action)
        if (intent.action != "android.intent.action.BOOT_COMPLETED" && intent.action != "android.intent.action.QUICKBOOT_POWERON") {
            return
        }
        context.startService(Intent(context, ResenderService::class.java))
    }
}
