// app/src/main/java/com/example/yonam_copspot/network/dto/ComplaintDoneDto.kt
package com.example.yonam_copspot.network.dto

import com.google.gson.annotations.SerializedName

/**
 * complaint_done 테이블 행 하나에 대한 응답
 * (여기선 개수 세는 용도라 필요한 필드만 둬도 됨)
 */
data class ComplaintDoneDto(
    @SerializedName("id") val id: Long,
    @SerializedName("user_id") val userId: String,
    @SerializedName("author_name") val authorName: String,
    @SerializedName("location_name") val locationName: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("done_at") val doneAt: String,
    @SerializedName("created_at") val createdAt: String?

)
