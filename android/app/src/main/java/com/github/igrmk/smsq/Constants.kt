package com.github.igrmk.smsq

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetReader

object Constants {
    const val DEFAULT_BASE_URL = "https://smsq.me"
    const val PREFERENCES = "com.github.igrmk.smsq.preferences"
    const val PREF_BASE_URL = "base_url"
    const val PREF_KEY = "key"
    const val PREF_ON = "on"
    const val PREF_CARRIER = "show_carrier"
    const val PREF_CONSENT = "consent"
    const val SOCKET_TIMEOUT_MS = 10000
    val RESEND_PERIOD_MS = arrayOf(5L * 60 * 1000, 15L * 60 * 1000, 45L * 60 * 1000)
    const val KEY_LENGTH = 64
    const val PERMISSIONS_SMS = 1
    const val PERMISSIONS_STATE = 2

    @Suppress("SpellCheckingInspection")
    const val PUBLIC_KEY_STRING = """
        {
            "primaryKeyId": 437945208,
            "key": [{
                "keyData": {
                    "typeUrl": "type.googleapis.com/google.crypto.tink.EciesAeadHkdfPublicKey",
                    "keyMaterialType": "ASYMMETRIC_PUBLIC",
                    "value": "EkQKBAgCEAMSOhI4CjB0eXBlLmdvb2dsZWFwaXMuY29tL2dvb2dsZS5jcnlwdG8udGluay5BZXNHY21LZXkSAhAQGAEYARohAL5Hc2sNbnpUuQeeWIfKEl+z2kK3GJ0l89k7mLqPRThgIiAOIT5bEubS/FLebsJ7usAsxxIIjNCXVj3975enYg1ssA=="
                },
                "outputPrefixType": "TINK",
                "keyId": 437945208,
                "status": "ENABLED"
            }]
        }
    """

    val PUBLIC_KEY = CleartextKeysetHandle.read(JsonKeysetReader.withBytes(PUBLIC_KEY_STRING.toByteArray()))!!
}
