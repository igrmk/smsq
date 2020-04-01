package com.github.igrmk.smsq.helpers

import android.os.Handler
import android.os.HandlerThread
import com.github.igrmk.smsq.BuildConfig
import java.net.*

class UdpLogger(url: String, port: Int) {
    private var handler: Handler
    private val handlerThread = HandlerThread(this::class.simpleName)
    private val datagramSocket by lazy { DatagramSocket() }
    private val address by lazy { InetSocketAddress(url, port) }

    init {
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    fun log(text: String) {
        val bytes = "$text\n".toByteArray()
        handler.post { send(bytes) }
    }

    private fun send(bytes: ByteArray) {
        val pkt = DatagramPacket(bytes, bytes.size, address)
        datagramSocket.send(pkt)
    }
}

class PrefixLogger(private val udpLogger: UdpLogger, private val prefix: String) {
    fun log(text: String) {
        udpLogger.log("[$prefix] $text")
    }
}

fun UdpLogger.pfx(prefix: String): PrefixLogger? = PrefixLogger(this, prefix)
