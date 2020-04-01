package com.github.igrmk.smsq.entities

open class Sms() {
    var id: Int = -1
    var text: String = ""
    var sim: String = ""
    var carrier: String = ""
    var sender: String = ""
    var timestamp: Long = 0
    var offset: Int = 0

    constructor(sms: Sms) : this() {
        id = sms.id
        text = sms.text
        sim = sms.sim
        carrier = sms.carrier
        sender = sms.sender
        timestamp = sms.timestamp
        offset = sms.offset
    }
}
