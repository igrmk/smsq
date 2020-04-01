package com.github.igrmk.smsq

object Constants {
    const val DEFAULT_BASE_URL = "https://smsq.me"
    const val PREFERENCES = "com.github.igrmk.smsq.preferences"
    const val PREF_BASE_URL = "base_url"
    const val PREF_KEY = "key"
    const val PREF_ON = "on"
    const val PREF_CARRIER = "show_carrier"
    const val SOCKET_TIMEOUT_MS = 10000
    val RESEND_PERIOD_MS = arrayOf(5L * 60 * 1000, 15L * 60 * 1000, 45L * 60 * 1000)
    const val KEY_LENGTH = 64
    const val PERMISSIONS_SMS = 1
    const val PERMISSIONS_STATE = 2
}
