package com.github.igrmk.smsq.entities

import kotlinx.serialization.Serializable

@Serializable
class SmsRequest : Sms {
    constructor(sms: Sms) : super(sms)
    var key: String = ""
}
