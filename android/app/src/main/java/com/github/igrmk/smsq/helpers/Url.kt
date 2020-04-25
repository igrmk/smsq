package com.github.igrmk.smsq.helpers

import com.github.igrmk.smsq.BuildConfig

@Suppress("ConstantConditionIf")
fun apiUrl(baseUrl: String) = if (BuildConfig.BUILD_TYPE == "staging") "https://apitest.$baseUrl/v1" else "https://api.$baseUrl/v1"
fun postSmsUrl(baseUrl: String) = "${apiUrl(baseUrl)}/sms"
