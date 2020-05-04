package com.github.igrmk.smsq

import com.github.igrmk.smsq.entities.BasicResponse
import com.github.igrmk.smsq.entities.DeliveryResult
import com.github.igrmk.smsq.entities.DeliveryResultSerializer
import com.github.igrmk.smsq.entities.SmsResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import org.junit.Test
import org.junit.Assert.*

class SerializeUnitTest {
    private val json = Json(JsonConfiguration.Stable, context = SerializersModule {
        polymorphic(BasicResponse::class) {
            SmsResponse::class with SmsResponse.serializer()
        }
    })

    @Test
    fun serializeCorrect() {
        assertEquals(""""delivered"""", json.stringify(DeliveryResultSerializer, DeliveryResult.Delivered))
    }
}
