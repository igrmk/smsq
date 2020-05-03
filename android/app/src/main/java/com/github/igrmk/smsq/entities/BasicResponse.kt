package com.github.igrmk.smsq.entities

interface BasicResponse<T> {
    var error: String?
    var result: T?
}

