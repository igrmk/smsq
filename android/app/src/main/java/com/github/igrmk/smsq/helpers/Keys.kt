package com.github.igrmk.smsq.helpers

import android.content.Context
import com.github.igrmk.smsq.Constants
import java.security.MessageDigest


@Suppress("SpellCheckingInspection")
private const val chars = "abcdefghijklmnopqrstuvwxyz"

@Suppress("SameParameterValue")
private fun randomString(length: Int) = (1..length).map { chars.random() }.joinToString("")

fun sha256(what: ByteArray): ByteArray {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(what)
    return md.digest()
}

private fun randomKey(): String {
    while (true) {
        val candidate = randomString(Constants.KEY_LENGTH)
        val sha = sha256(sha256(candidate.toByteArray()))
        if (sha[0].toInt() == 0 && sha[1].toInt().and(0xf0) == 0) {
            return candidate
        }
    }
}

fun Context.updateKey() {
    myPreferences.key = randomKey()
}

fun Context.revokeKey() {
    myPreferences.key = null
}

