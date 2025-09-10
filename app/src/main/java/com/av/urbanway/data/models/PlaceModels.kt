package com.av.urbanway.data.models

data class PlaceResult(
    val placeId: String?,
    val title: String,
    val subtitle: String?,
    val coordinates: Coordinates?
)

data class PlaceDetails(
    val placeId: String?,
    val name: String,
    val address: String?,
    val coordinates: Coordinates,
    val phoneNumber: String? = null,
    val website: String? = null
)

