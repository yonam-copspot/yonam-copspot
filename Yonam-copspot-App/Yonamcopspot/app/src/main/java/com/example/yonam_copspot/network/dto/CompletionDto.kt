// app/src/main/java/com/example/yonam_copspot/network/dto/CompletionDto.kt
package com.example.yonam_copspot.network.dto

import com.google.gson.annotations.SerializedName

/**
 * /api/mobile/completions 응답 한 건
 *
 * 서버 JSON 예시는 대략:
 * {
 *   "id": 1,
 *   "userId": "22360006" 또는 "user_id": "...",
 *   "createdAt": "...",  또는 "created_at": "...",
 *   "doneAt": "...",     또는 "done_at": "...",
 *   "locationName": "...", 또는 "location_name": "...",
 *   "description": "..."
 * }
 */
data class CompletionDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName(value = "user_id", alternate = ["userId"])
    val userId: String?,

    @SerializedName(value = "registered_at", alternate = ["createdAt"])
    val createdAt: String?,

    @SerializedName(value = "completed_at", alternate = ["doneAt"])
    val doneAt: String?,

    @SerializedName(value = "address", alternate = ["locationName"])
    val locationName: String?,

    @SerializedName("description")
    val description: String?
)
