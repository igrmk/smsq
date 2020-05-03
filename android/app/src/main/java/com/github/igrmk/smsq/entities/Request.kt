package com.github.igrmk.smsq.entities

import kotlinx.serialization.Serializable

@Serializable
class Request {
    var payload: String? = null
    var version: Int = 1
}
