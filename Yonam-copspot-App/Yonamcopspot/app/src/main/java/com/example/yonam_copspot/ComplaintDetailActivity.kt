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

    private var complaint: MyComplaintUiModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complaint_status)

        supportActionBar?.title = "민원 상세"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 뷰 바인딩
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

        // 인텐트로 넘어온 MyComplaintUiModel 받기
        complaint = getComplaintFromIntent()

        if (complaint == null) {
            Toast.makeText(this, "민원 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindComplaint(complaint!!)
    }

    /**
     * API 레벨에 따라 Serializable 꺼내는 방법 다르게 처리
     */
    private fun getComplaintFromIntent(): MyComplaintUiModel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("complaint", MyComplaintUiModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("complaint") as? MyComplaintUiModel
        }
    }

    /**
     * UI에 데이터 바인딩
     */
    private fun bindComplaint(item: MyComplaintUiModel) {
        textLocation.text = item.locationName ?: "장소 정보 없음"
        textDescription.text = item.description ?: "상세 내용 없음"

        textCreatedAt.text = "등록일시: ${item.createdAt}"
        textDoneAt.text = "완료일시: ${item.doneAt ?: "-"}"

        if (item.isDone) {
            textStatus.text = "완료"
        } else {
            textStatus.text = "진행중"
        }

        // 댓글 목록
        val comments: List<CommentDto> = item.comments
        val count = comments.size
        textCommentCount.text = "댓글 ${count}개"

        commentAdapter.submitList(comments)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
