package com.github.igrmk.smsq.helpers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.telephony.SmsMessage
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.app.ActivityCompat
import com.github.igrmk.smsq.entities.Sms
import com.github.igrmk.smsq.services.ResenderService
import java.util.*


class SmsReceiver : BroadcastReceiver() {
    private val tag = this::class.simpleName!!

    private fun simInfo(context: Context, slot: Int): SubscriptionInfo? {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            return subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(slot)
        }
        return null
    }

    override fun onReceive(context: Context, intent: Intent) {
        context.linf(tag, "action received: ${intent.action}, on: ${context.myPreferences.on}")
        if (!context.myPreferences.on) {
            return
        }

        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") {
            return
        }

        val extras = intent.extras!!
        val slot = extras.getInt("slot_id", -1)
        val simInfo = if (slot >= 0) simInfo(context, slot) else null
        var displayName = ""
        var carrierName = ""
        if (context.myPreferences.showCarrier && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            displayName = simInfo?.displayName?.toString() ?: ""
            carrierName = simInfo?.carrierName?.toString() ?: ""
        }
        val pdus = extras["pdus"] as Array<*>
        val parts = pdus.map {
            val bytes = it as ByteArray
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                SmsMessage.createFromPdu(bytes, extras.getString("format"))
            else
                SmsMessage.createFromPdu(bytes)
        }
        if (parts.isEmpty()) {
            context.lerr(tag, "empty message")
            return
        }
        val text = parts.joinToString(separator = "") { it!!.messageBody }
        val address = parts[0]!!.originatingAddress!!
        val timestamp = parts[0]!!.timestampMillis

        val cal = GregorianCalendar()
        val tz = cal.timeZone
        val offset = tz.getOffset(timestamp)

        val sms = Sms().apply {
            this.sim = displayName
            this.carrier = carrierName
            this.text = text
            this.sender = address
            this.timestamp = timestamp / 1000
            this.offset = offset / 1000
        }

        context.storeSms(sms)
        context.startService(Intent(context, ResenderService::class.java))
    }
}
