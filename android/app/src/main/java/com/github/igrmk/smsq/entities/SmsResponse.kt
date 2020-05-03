package com.github.igrmk.smsq.entities

import kotlinx.serialization.*

@Serializable
class SmsResponse : BasicResponse<DeliveryResult> {
    override var error: String? = null
    override var result: DeliveryResult? = null
}
