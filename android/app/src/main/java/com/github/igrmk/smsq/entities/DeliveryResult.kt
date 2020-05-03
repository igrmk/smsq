package com.github.igrmk.smsq.entities

import android.annotation.SuppressLint
import kotlinx.serialization.*

@Serializable(with = DeliveryResultSerializer::class)
enum class DeliveryResult {
    Unknown,
    Delivered,
    NetworkError,
    Blocked,
    BadRequest,
    UserNotFound,
    ApiRetired,
    RateLimited;

    override fun toString(): String {
        return when (this) {
            Unknown -> "unknown"
            Delivered -> "delivered"
            NetworkError -> "network_error"
            Blocked -> "blocked"
            BadRequest -> "bad_request"
            UserNotFound -> "user_not_found"
            ApiRetired -> "api_retired"
            RateLimited -> "rate_limited"
        }
    }
}

@Serializer(forClass = DeliveryResult::class)
object DeliveryResultSerializer : KSerializer<DeliveryResult> {
    private val className = this::class.qualifiedName!!
    private val lookup = DeliveryResult.values().map { it.toString() to it }.toMap()
    override val descriptor = PrimitiveDescriptor(className, PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: DeliveryResult) = encoder.encodeString(value.toString())

    // Suppressed due to the bug in the analyzer
    @SuppressLint("NewApi")
    override fun deserialize(decoder: Decoder) = lookup.getOrDefault(decoder.decodeString(), DeliveryResult.Unknown)
}
