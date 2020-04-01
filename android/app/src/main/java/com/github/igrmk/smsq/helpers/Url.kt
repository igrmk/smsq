package com.github.igrmk.smsq.helpers

fun apiUrl(baseUrl: String) = "$baseUrl/api"
fun postSmsUrl(baseUrl: String) = apiUrl(baseUrl) + "/sms"
