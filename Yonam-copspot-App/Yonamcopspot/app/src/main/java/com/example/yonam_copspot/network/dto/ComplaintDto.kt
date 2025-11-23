// app/src/main/java/com/example/yonam_copspot/network/dto/ComplaintDto.kt
package com.example.yonam_copspot.network.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * (구) 진행 중 민원 한 건을 표현하는 DTO
 * GET /api/complaints?userId=... 응답용
 *
 * 필요 필드:
 *  - id
 *  - user_id
 *  - author_name
 *  - photo_base64
 *  - location_name
 *  - description
 *  - latitude
 *  - longitude
 *  - created_at
 */
data class ComplaintDto(
    @SerializedName("id") val id: Long,
    @SerializedName("user_id") val userId: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("photo_base64") val photoBase64: String?,
    @SerializedName("location_name") val locationName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("created_at") val createdAt: String
) : Serializable
