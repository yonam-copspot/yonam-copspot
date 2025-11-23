// app/src/main/java/com/example/yonam_copspot/MyComplaintsActivity.kt
package com.example.yonam_copspot

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yonam_copspot.network.RetrofitClient
import com.example.yonam_copspot.ui.MyComplaintUiModel
import kotlinx.coroutines.launch

class MyComplaintsActivity : AppCompatActivity() {

    // TODO: 실제 로그인 시스템 생기면 여기서 userId 받아오기
    private val loginUserId = "22360006"

    private lateinit var recyclerView: RecyclerView
    private lateinit var textEmpty: TextView
    private lateinit var adapter: MyComplaintsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        supportActionBar?.title = "나의 민원"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerViewMyComplaints)
        textEmpty = findViewById(R.id.textEmptyMyComplaints)

        adapter = MyComplaintsAdapter { item ->
            // 아이템 클릭 시: 상세 화면으로 이동 (댓글 포함)
            val intent = Intent(this, ComplaintDetailActivity::class.java).apply {
                putExtra("complaint", item)  // MyComplaintUiModel은 Serializable
            }
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadMyComplaints()
    }

    /**
     * /api/mobile/my-complaints?userId=... 호출해서
     * 진행중 + 완료 민원을 모두 가져와 리스트로 합침
     */
    private fun loadMyComplaints() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getMyComplaints(loginUserId)

                val uiList = mutableListOf<MyComplaintUiModel>()

                // 진행 중 민원
                resp.ongoing.forEach { c ->
                    uiList += MyComplaintUiModel(
                        id = c.id,
                        locationName = c.locationName,
                        description = c.description,
                        createdAt = c.createdAt,
                        doneAt = null,
                        isDone = false,
                        comments = c.comments
                    )
                }

                // 완료된 민원
                resp.done.forEach { c ->
                    uiList += MyComplaintUiModel(
                        id = c.id,
                        locationName = c.locationName,
                        description = c.description,
                        createdAt = c.createdAt,
                        doneAt = c.doneAt,
                        isDone = true,
                        comments = c.comments
                    )
                }

                if (uiList.isEmpty()) {
                    textEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    textEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.submitList(uiList)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MyComplaintsActivity,
                    "내 민원을 불러오지 못했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
