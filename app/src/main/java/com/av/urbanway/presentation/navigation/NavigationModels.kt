package com.av.urbanway.presentation.navigation

import com.av.urbanway.data.model.QueryType
import com.av.urbanway.data.model.TransitData

sealed class UrbanWayScreen {
    object Chat : UrbanWayScreen()

    data class MessageDetail(
        val messageId: String,
        val queryType: QueryType,
        val data: TransitData
    ) : UrbanWayScreen()
}

data class MessageDetailArgs(
    val messageId: String,
    val queryType: QueryType,
    val data: TransitData
)