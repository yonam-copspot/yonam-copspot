// app/src/main/java/com/example/yonam_copspot/network/dto/MyComplaintItemDto.kt
package com.example.yonam_copspot.network.dto

import com.google.gson.annotations.SerializedName

/**
 * /api/mobile/my-complaints 응답에서
 * 진행 중/완료 민원 각각의 "한 건"을 표현
 *
 * 진행 중(complaint)일 때:
 *  - done_at 이 null
 * 완료(complaint_done)일 때:
 *  - done_at 에 값 존재
 *
 * comments:
 *  - complaint_comment 또는 complaint_done_comment 에서 온 댓글 리스트
 */
data class MyComplaintsResponseDto(
    @SerializedName("ongoing") val ongoing: List<MyComplaintItemDto>,
    @SerializedName("done")    val done:    List<MyComplaintItemDto>
)