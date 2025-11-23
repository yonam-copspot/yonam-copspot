package com.example.yonam_copspot.network.dto

import com.google.gson.annotations.SerializedName

// MyComplaintItemDto.kt
data class MyComplaintItemDto(
    @SerializedName("id")          val id: Long,
    @SerializedName("user_id")     val userId: String,
    @SerializedName("name") val authorName: String,
    @SerializedName("adress") val locationName: String?,
    @SerializedName("detail") val description: String?,
    @SerializedName("registered_at")  val createdAt: String,
    @SerializedName("completed_at")     val doneAt: String?,
    @SerializedName("comments")    val comments: List<CommentDto>
)
