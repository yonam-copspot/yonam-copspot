package com.example.yonam_copspot

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
import kotlinx.coroutines.launch

class ComplaintStatusActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var textEmpty: TextView
    private lateinit var adapter: ComplaintStatusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 이 레이아웃 안에 recyclerViewCompletions / textEmptyCompletions 가 있어야 함
        setContentView(R.layout.activity_complaint_status)

        supportActionBar?.title = "민원 처리 현황"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.recyclerViewCompletions)
        textEmpty = findViewById(R.id.textEmptyCompletions)

        // ✅ 네 어댑터는 람다 받는 생성자가 아니니까 인자 없이 생성
        adapter = ComplaintStatusAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        loadCompletions()
    }

    private fun loadCompletions() {
        lifecycleScope.launch {
            try {
                val list = RetrofitClient.api.getCompletions()

                if (list.isEmpty()) {
                    textEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    textEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.submitList(list)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ComplaintStatusActivity,
                    "민원 처리 현황을 불러오지 못했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
