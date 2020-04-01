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
import kotlin.math.min

class ResenderService : Service() {
    private val tag = this::class.simpleName

    private lateinit var handler: Handler
    private val handlerThread = HandlerThread(this::class.simpleName)
    private lateinit var rest: RestTalker
    private val resendRunnable = Runnable { resend() }
    private var resendAttempt = 0

    override fun onBind(intent: Intent): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        handler.post {
            resendAttempt = 0
            handler.removeCallbacks(resendRunnable)
            handler.post(resendRunnable)
        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        rest = RestTalker(myPreferences.baseUrl)
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quit()
    }

    private fun resend() {
        if (send()) {
            stopSelf()
            return
        }
        val period = Constants.RESEND_PERIOD_MS
        handler.postDelayed(resendRunnable, period[min(resendAttempt, period.size - 1)])
        resendAttempt++
    }

    private fun send(): Boolean {
        val key = myPreferences.key ?: return true
        for (i in allSmses()) {
            val req = SmsRequest(i).apply { this.key = key }
            val res = rest.postSms(req)
            if (!res.second || res.first == DeliveryResult.NetworkError) {
                return false
            }
            deleteSms(req.id)
        }
        return true
    }
}
