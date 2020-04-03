package com.github.igrmk.smsq.helpers

fun apiUrl(baseUrl: String) = "$baseUrl/api/v1"
fun postSmsUrl(baseUrl: String) = apiUrl(baseUrl) + "/sms"
