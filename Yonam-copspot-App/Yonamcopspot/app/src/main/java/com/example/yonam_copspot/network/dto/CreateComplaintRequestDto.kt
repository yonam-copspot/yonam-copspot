// app/src/main/java/com/example/yonam_copspot/network/dto/CreateComplaintRequestDto.kt
package com.example.yonam_copspot.network.dto

import com.google.gson.annotations.SerializedName

/**
 * POST /api/complaints 요청 바디
 *
 * DB complaint 테이블 기준:
 *  - user_id
 *  - author_name
 *  - photo_base64
 *  - location_name
 *  - description (상세 설명)
 *  - latitude
 *  - longitude
 */
data class CreateComplaintRequestDto(
    @SerializedName("user_id") val userId: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("photo_base64") val photoBase64: String?,
    @SerializedName("location_name") val locationName: String?,
    @SerializedName("description") val description: String,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)
