// app/src/main/java/com/example/yonam_copspot/ComplaintDetailActivity.kt
package com.example.yonam_copspot

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yonam_copspot.network.dto.CommentDto
import com.example.yonam_copspot.ui.MyComplaintUiModel

class ComplaintDetailActivity : AppCompatActivity() {

    private lateinit var textLocation: TextView
    private lateinit var textStatus: TextView
    private lateinit var textCreatedAt: TextView
    private lateinit var textDoneAt: TextView
    private lateinit var textDescription: TextView
    private lateinit var textCommentCount: TextView

    private lateinit var recyclerViewComments: RecyclerView
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 너가 맞췄다고 한 “디테일 레이아웃”으로 변경
        setContentView(R.layout.activity_complaint_detail)

        supportActionBar?.title = "민원 상세"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textLocation = findViewById(R.id.textDetailLocation)
        textStatus = findViewById(R.id.textDetailStatus)
        textCreatedAt = findViewById(R.id.textDetailCreatedAt)
        textDoneAt = findViewById(R.id.textDetailDoneAt)
        textDescription = findViewById(R.id.textDetailDescription)
        textCommentCount = findViewById(R.id.textDetailCommentCount)

        recyclerViewComments = findViewById(R.id.recyclerViewComments)
        commentAdapter = CommentAdapter()
        recyclerViewComments.layoutManager = LinearLayoutManager(this)
        recyclerViewComments.adapter = commentAdapter

        val complaint = getComplaintFromIntent()
        if (complaint == null) {
            Toast.makeText(this, "민원 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindComplaint(complaint)
    }

    private fun getComplaintFromIntent(): MyComplaintUiModel? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("complaint", MyComplaintUiModel::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra("complaint") as? MyComplaintUiModel
            }
        } catch (e: Exception) {
            // ✅ Serializable 문제로 여기서 터지면 “안 넘어가는 것처럼” 보임
            e.printStackTrace()
            null
        }
    }

    private fun bindComplaint(item: MyComplaintUiModel) {
        textLocation.text = item.locationName ?: "장소 정보 없음"
        textDescription.text = item.description ?: "상세 내용 없음"
        textCreatedAt.text = "등록일시: ${item.createdAt}"
        textDoneAt.text = "완료일시: ${item.doneAt ?: "-"}"
        textStatus.text = if (item.isDone) "완료" else "진행중"

        val comments: List<CommentDto> = item.comments
        textCommentCount.text = "댓글 ${comments.size}개"
        commentAdapter.submitList(comments)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
    }
}
