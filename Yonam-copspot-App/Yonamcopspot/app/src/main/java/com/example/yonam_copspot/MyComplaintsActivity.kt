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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.yonam_copspot.network.RetrofitClient
import com.example.yonam_copspot.network.dto.MyComplaintItemDto
import com.example.yonam_copspot.ui.MyComplaintUiModel
import kotlinx.coroutines.launch

class MyComplaintsActivity : AppCompatActivity() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var textEmpty: TextView
    private lateinit var adapter: MyComplaintsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_complaints)

        supportActionBar?.title = "나의 민원"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        swipeRefresh = findViewById(R.id.swipeRefreshMyComplaints)
        recyclerView = findViewById(R.id.recyclerViewMyComplaints)
        textEmpty = findViewById(R.id.textEmptyMyComplaints)

        // ✅ 클릭하면 무조건 상세로 이동
        adapter = MyComplaintsAdapter { item ->
            val intent = Intent(this, ComplaintDetailActivity::class.java)
            intent.putExtra("complaint", item) // MyComplaintUiModel이 Serializable이어야 함
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        swipeRefresh.setOnRefreshListener { loadMyComplaints() }

        loadMyComplaints()
    }

    override fun onResume() {
        super.onResume()
        loadMyComplaints() // ✅ 화면 돌아오면 자동 갱신
    }

    private fun loadMyComplaints() {
        swipeRefresh.isRefreshing = true

        lifecycleScope.launch {
            try {
                val resp: List<MyComplaintItemDto> =
                    RetrofitClient.api.getMyComplaints(LoginSession.userId)

                val uiList: List<MyComplaintUiModel> = resp.map { c ->
                    MyComplaintUiModel(
                        id = c.id,
                        locationName = c.locationName,
                        description = c.description,
                        createdAt = c.createdAt,
                        doneAt = c.doneAt,
                        isDone = c.doneAt != null,
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
                Toast.makeText(this@MyComplaintsActivity, "내 민원을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish(); true
        } else super.onOptionsItemSelected(item)
    }
}
