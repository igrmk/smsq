package com.github.igrmk.smsq.entities

import android.annotation.SuppressLint
import kotlinx.serialization.*

@Serializable(with = DeliveryResultSerializer::class)
enum class DeliveryResult {
    @SerialName("unknown")
    Unknown,

    @SerialName("delivered")
    Delivered,

    @SerialName("network_error")
    NetworkError,

    @SerialName("blocked")
    Blocked,

    @SerialName("bad_request")
    BadRequest,

    @SerialName("user_not_found")
    UserNotFound,

    @SerialName("api_retired")
    ApiRetired,

    @SerialName("rate_limited")
    RateLimited
}

val DeliveryResult.serialName: String
    get() = this::class.java.getField(this.name).getAnnotation(SerialName::class.java)!!.value

@Serializer(forClass = DeliveryResult::class)
object DeliveryResultSerializer : KSerializer<DeliveryResult> {
    private val className = this::class.qualifiedName!!
    private val lookup = DeliveryResult.values().associateBy({ it }, { it.serialName })
    private val revLookup = DeliveryResult.values().associateBy { it.serialName }
    override val descriptor = PrimitiveDescriptor(className, PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: DeliveryResult) = encoder.encodeString(lookup.getValue(value))

    // Suppressed due to the bug in the analyzer
    @SuppressLint("NewApi")
    override fun deserialize(decoder: Decoder) = revLookup.getOrDefault(decoder.decodeString(), DeliveryResult.Unknown)
}
