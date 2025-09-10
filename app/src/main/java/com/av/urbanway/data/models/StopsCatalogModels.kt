package com.av.urbanway.data.models

import com.google.gson.annotations.SerializedName

data class StopChangePayload(
    @SerializedName("change_type") val changeType: String,
    @SerializedName("stop_id") val stopId: String,
    @SerializedName("stop_data") val stopDataJson: String,
    @SerializedName("change_date") val changeDate: String
)

data class InnerStopData(
    @SerializedName("stop_id") val stopId: String,
    @SerializedName("stop_code") val stopCode: String,
    @SerializedName("stop_name") val stopName: String,
    @SerializedName("stop_desc") val stopDesc: String?,
    @SerializedName("stop_lat") val stopLat: Double,
    @SerializedName("stop_lon") val stopLon: Double
)
