// app/src/main/java/com/example/yonam_copspot/ui/MyComplaintUiModel.kt
package com.example.yonam_copspot.ui

import com.example.yonam_copspot.network.dto.CommentDto
import java.io.Serializable

/**
 * '나의 민원' 리스트에서 사용하기 위한 UI용 모델
 *
 * - 진행 중/완료 민원 공통으로 사용
 * - 댓글 목록까지 포함
 */
data class MyComplaintUiModel(
    val id: Long,
    val locationName: String?,
    val description: String?,
    val createdAt: String,
    val doneAt: String?,
    val isDone: Boolean,
    val comments: List<CommentDto>
) : Serializable {

    val commentCount: Int
        get() = comments.size
}
