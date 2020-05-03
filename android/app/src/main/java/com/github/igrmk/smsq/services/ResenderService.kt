package com.github.igrmk.smsq.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import com.github.igrmk.smsq.Constants
import com.github.igrmk.smsq.entities.DeliveryResult
import com.github.igrmk.smsq.entities.SmsRequest
import com.github.igrmk.smsq.helpers.*
import com.google.crypto.tink.HybridEncrypt
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlin.math.min

class ResenderService : Service() {
    private val tag = this::class.simpleName!!

    private lateinit var handler: Handler
    private val handlerThread = HandlerThread(this::class.simpleName)
    private lateinit var rest: RestTalker
    private val resendRunnable = Runnable { resend() }
    private var resendAttempt = 0

    override fun onBind(intent: Intent): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        linf(tag, "starting service...")
        super.onStartCommand(intent, flags, startId)
        handler.post {
            resendAttempt = 0
            handler.removeCallbacks(resendRunnable)
            handler.post(resendRunnable)
        }
        return START_STICKY
    }

    override fun onCreate() {
        linf(tag, "creating service...")
        super.onCreate()
        rest = RestTalker(this.logger(), myPreferences.domainName, Constants.PUBLIC_KEY.getPrimitive(HybridEncrypt::class.java))
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    override fun onDestroy() {
        linf(tag, "destroying service...")
        super.onDestroy()
        handlerThread.quit()
    }

    private fun resend() {
        linf(tag, "resending...")
        if (send()) {
            linf(tag, "resending done")
            stopSelf()
            return
        }
        val periods = Constants.RESEND_PERIOD_MS
        val period = periods[min(resendAttempt, periods.size - 1)]
        linf(tag, "let us retry send in $period ms")
        handler.postDelayed(resendRunnable, period)
        resendAttempt++
    }

    private fun send(): Boolean {
        val key = myPreferences.key ?: return true
        for (i in allSmses()) {
            val req = SmsRequest(i).apply { this.key = key }
            val (deliveryResult, gotReply) = rest.postSms(req)
            if (!gotReply) {
                linf(tag, "send failed")
                return false
            }
            if (deliveryResult == null) {
                linf(tag, "send result: API error")
                return false
            }
            linf(tag, "send result: $deliveryResult")
            if (deliveryResult == DeliveryResult.NetworkError) {
                return false
            }
            if (deliveryResult == DeliveryResult.ApiRetired) {
                myPreferences.retired = true
                deleteAllSmses()
                return true
            }
            linf(tag, "SMS processed, removing...")
            deleteSms(req.id)
        }
        return true
    }
}
