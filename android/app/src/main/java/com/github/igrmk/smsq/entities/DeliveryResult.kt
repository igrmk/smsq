package com.github.igrmk.smsq.entities

import com.google.gson.annotations.SerializedName

enum class DeliveryResult {
    @SerializedName("delivered")
    Delivered,

    @SerializedName("network_error")
    NetworkError,

    @SerializedName("blocked")
    Blocked,

    @SerializedName("bad_request")
    BadRequest,

    @SerializedName("user_not_found")
    UserNotFound,

    @SerializedName("api_retired")
    ApiRetired
}
