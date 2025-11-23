// app/src/main/java/com/example/yonam_copspot/network/dto/DoneResponseDto.kt
package com.example.yonam_copspot.network.dto

import com.google.gson.annotations.SerializedName

/**
 * (구) /api/complaints/{id}/done 호출 결과용 DTO
 * 사용 예: {"success": true, "message": "ok"}
 */
data class DoneResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?
)
