// app/src/main/java/com/example/yonam_copspot/network/dto/CommentDto.kt
package com.example.yonam_copspot.network.dto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * 나의 민원에 달린 댓글 한 건
 *
 * complaint_comment / complaint_done_comment 공통 필드:
 * - commenter_name
 * - comment_text
 * - created_at
 */
data class CommentDto(
    @SerializedName("commenter_name") val commenterName: String,
    @SerializedName("comment_text") val commentText: String,
    @SerializedName("created_at") val createdAt: String
) : Serializable
