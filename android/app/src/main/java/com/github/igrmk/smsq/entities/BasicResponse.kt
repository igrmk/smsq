package com.github.igrmk.smsq.entities

open class BasicResponse<T> {
    var error: String? = null
    var result: T? = null
}