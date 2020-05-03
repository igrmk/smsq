package com.github.igrmk.smsq.entities

import kotlinx.serialization.*

@Serializable(with = DeliveryResultSerializer::class)
enum class DeliveryResult {
    Unknown,
    Delivered,
    NetworkError,
    Blocked,
    BadRequest,
    UserNotFound,
    ApiRetired;

    override fun toString(): String {
        return when (this) {
            Unknown -> "unknown"
            Delivered -> "delivered"
            NetworkError -> "network_error"
            Blocked -> "blocked"
            BadRequest -> "bad_request"
            UserNotFound -> "user_not_found"
            ApiRetired -> "api_retired"
        }
    }
}

@Serializer(forClass = DeliveryResult::class)
object DeliveryResultSerializer : KSerializer<DeliveryResult> {
    private val lookup = DeliveryResult.values().map { it.toString() to it }.toMap()
    override val descriptor = PrimitiveDescriptor(this::class.simpleName!!, PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: DeliveryResult) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder) = lookup.getOrDefault(decoder.decodeString(), DeliveryResult.Unknown)
}
