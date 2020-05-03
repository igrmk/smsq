package com.github.igrmk.smsq

import com.github.igrmk.smsq.entities.DeliveryResult
import com.github.igrmk.smsq.entities.SmsResponse
import com.github.igrmk.smsq.helpers.NopLogger
import com.github.igrmk.smsq.helpers.RestTalker
import com.github.kittinunf.fuel.core.FuelError
import org.junit.Test
import org.junit.Assert.*
import java.net.URL
import com.github.kittinunf.result.Result

class ParseUnitTest {
    private var rest = RestTalker(NopLogger(), "", null)

    @Test
    fun parsingCorrect() {
        val data = Result.Success<String, FuelError>("""{ "result": "delivered" }""")
        val parsed = rest.checkErrors(SmsResponse.serializer(), URL("https://example.com"), data)
        val result = parsed.first
        assertEquals(DeliveryResult.Delivered, result)
        assertEquals(true, parsed.second)
    }

    @Test
    fun parsingUnknownEnum() {
        val data = Result.Success<String, FuelError>("""{ "result": "absolutely_unknown" }""")
        val parsed = rest.checkErrors(SmsResponse.serializer(), URL("https://example.com"), data)
        assertEquals(DeliveryResult.Unknown, parsed.first)
        assertEquals(true, parsed.second)
    }

    @Test
    fun parsingNumberAsEnum() {
        val data = Result.Success<String, FuelError>("""{ "result": 1 }""")
        val parsed = rest.checkErrors(SmsResponse.serializer(), URL("https://example.com"), data)
        assertEquals(null, parsed.first)
        assertEquals(false, parsed.second)
    }

    @Test
    fun parsingObjectAsEnum() {
        val data = Result.Success<String, FuelError>("""{ "result": { result: "" } }""")
        val parsed = rest.checkErrors(SmsResponse.serializer(), URL("https://example.com"), data)
        assertEquals(null, parsed.first)
        assertEquals(false, parsed.second)
    }
}
