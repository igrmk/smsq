package com.github.igrmk.smsq.helpers

import com.github.igrmk.smsq.Constants
import com.github.igrmk.smsq.entities.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.result.Result
import com.google.crypto.tink.HybridEncrypt
import com.google.gson.Gson
import com.google.gson.JsonParseException

class RestTalker(private val baseUrl: String) {
    private val tag = this::class.simpleName!!
    private val gson = Gson()
    private val hybridEncrypt = Constants.PUBLIC_KEY.getPrimitive(HybridEncrypt::class.java)

    private inline fun <U, reified T : BasicResponse<U>> checkErrors(request: Request, result: Result<String, FuelError>): Pair<U?, Boolean> {
        val obj: BasicResponse<U>
        val (data, error) = result
        ldbg(tag, "sent request: '${request.url}'")
        ldbg(tag, "got response: '$data'")
        if (error != null) {
            return Pair(null, false)
        }
        try {
            obj = gson.fromJson(data, T::class.java)
        } catch (_: JsonParseException) {
            lerr(tag, "REST API error: cannot parse")
            return Pair(null, false)
        }
        if (obj.error != null || obj.result == null) {
            lerr(tag, "REST API error: ${obj.error}")
            return Pair(null, true)
        }
        return Pair(obj.result, true)
    }

    fun postSms(data: SmsRequest): Pair<DeliveryResult?, Boolean> {
        val json = gson.toJson(data)
        val request = Request().apply {
            this.payload = encrypt(json)
        }
        val (req, _, result) = Fuel
                .post(postSmsUrl(baseUrl))
                .body(gson.toJson(request))
                .header("Content-Type" to "application/json")
                .timeout(Constants.SOCKET_TIMEOUT_MS)
                .timeoutRead(Constants.SOCKET_TIMEOUT_MS)
                .responseString()
        return checkErrors<DeliveryResult, SmsResponse>(req, result)
    }

    private fun encrypt(str: String): String {
        val encrypted = hybridEncrypt.encrypt(str.toByteArray(), null)
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT)
    }
}
