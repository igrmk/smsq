package com.github.igrmk.smsq.helpers

fun apiUrl(baseUrl: String) = "https://api.$baseUrl/v1"
fun postSmsUrl(baseUrl: String) = "${apiUrl(baseUrl)}/sms"
