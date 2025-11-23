// app/src/main/java/com/example/yonam_copspot/network/CopspotApi.kt
package com.example.yonam_copspot.network

import com.example.yonam_copspot.network.dto.ComplaintDoneDto
import com.example.yonam_copspot.network.dto.ComplaintDto
import com.example.yonam_copspot.network.dto.CompletionDto
import com.example.yonam_copspot.network.dto.CreateComplaintRequestDto
import com.example.yonam_copspot.network.dto.DoneResponseDto
import com.example.yonam_copspot.network.dto.MyComplaintsResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CopspotApi {

    /**
     * 민원 생성
     * POST http://182.31.216.234:3000/api/create
     */
    @POST("api/create")
    suspend fun createComplaint(
        @Body body: CreateComplaintRequestDto
    )

    /**
     * (이전) 진행 중 민원 목록
     * GET /api/complaints?userId=...
     * - 기존 코드가 참조하고 있을 수 있어서 일단 유지
     */
    @GET("api/complaints")
    suspend fun getComplaints(
        @Query("userId") userId: String
    ): List<ComplaintDto>

    /**
     * (이전) 완료된 민원 목록
     * GET /api/complaints-done?userId=...
     * - 기존 코드 호환용으로 유지
     */
    @GET("api/complaints-done")
    suspend fun getDoneComplaints(
        @Query("userId") userId: String
    ): List<ComplaintDoneDto>

    /**
     * (이전) 민원 처리 완료 표시
     * POST /api/complaints/{id}/done
     * - 여전히 쓸 수 있도록 남겨둠
     */
    @POST("api/complaints/{id}/done")
    suspend fun markComplaintDone(
        @Path("id") complaintId: Long
    ): DoneResponseDto

    /**
     * 민원 처리 현황(완료된 민원 전체)
     * GET /api/mobile/completions
     *
     * id, user_id, created_at, done_at, location_name, description
     */
    @GET("api/mobile/completions")
    suspend fun getCompletions(): List<CompletionDto>

    /**
     * 나의 민원 (진행 + 완료 + 댓글)
     * GET /api/mobile/my-complaints?userId=...
     *
     * - userId를 쿼리 파라미터로 보내서, 그 아이디 기준으로 DB 조회
     */
    @GET("api/mobile/my-complaints")
    suspend fun getMyComplaints(
        @Query("user_id") userId: String
    ): MyComplaintsResponseDto
}
