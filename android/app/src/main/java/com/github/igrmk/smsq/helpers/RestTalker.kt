package com.github.igrmk.smsq.helpers

import com.github.igrmk.smsq.Constants
import com.github.igrmk.smsq.entities.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.result.Result
import com.google.crypto.tink.HybridEncrypt
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonDecodingException
import kotlinx.serialization.modules.SerializersModule
import java.net.URL

class RestTalker(val logger: Logger, private val baseUrl: String, private val hybridEncrypt: HybridEncrypt?) {
    val tag = this::class.simpleName!!
    val json = Json(JsonConfiguration.Stable, context = SerializersModule {
        polymorphic(BasicResponse::class) {
            SmsResponse::class with SmsResponse.serializer()
        }
    })

    inline fun <U, reified T : BasicResponse<U>> checkErrors(serializer: DeserializationStrategy<T>, requestUrl: URL, result: Result<String, FuelError>): Pair<U?, Boolean> {
        val obj: BasicResponse<U>?
        val (data, error) = result
        logger.linf(tag, "sent request: '${requestUrl}'")
        if (data == null) {
            return Pair(null, false)
        }
        if (error == null) {
            logger.linf(tag, "got response: '$data'")
        }
        if (error != null) {
            logger.linf(tag, "got error: $error")
            return Pair(null, false)
        }
        try {
            obj = json.parse(serializer, data)
        } catch (ex: JsonDecodingException) {
            logger.lerr(tag, "REST API error: cannot parse")
            return Pair(null, false)
        }
        if (obj.error != null || obj.result == null) {
            logger.lerr(tag, "REST API error: ${obj.error}")
            return Pair(null, true)
        }
        return Pair(obj.result, true)
    }

    fun postSms(data: SmsRequest): Pair<DeliveryResult?, Boolean> {
        val (req, _, result) = Fuel
                .post(postSmsUrl(baseUrl))
                .body(body(SmsRequest.serializer(), data))
                .header("Content-Type" to "application/json")
                .timeout(Constants.SOCKET_TIMEOUT_MS)
                .timeoutRead(Constants.SOCKET_TIMEOUT_MS)
                .responseString()
        return checkErrors(SmsResponse.serializer(), req.url, result)
    }

    private fun <T> body(serializer: SerializationStrategy<T>, data: T): String {
        val payloadJson = json.stringify(serializer, data)
        val request = Request().apply { this.payload = encrypt(payloadJson) }
        return json.stringify(Request.serializer(), request)
    }

    private fun encrypt(str: String): String {
        val encrypted = hybridEncrypt!!.encrypt(str.toByteArray(), null)
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT)
    }
}
