// app/src/main/java/com/example/yonam_copspot/MainActivity.kt
package com.example.yonam_copspot

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.yonam_copspot.network.RetrofitClient
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val loginUserId = "22360006"

    private lateinit var textSummaryContent: TextView
    private lateinit var btnCreateComplaint: LinearLayout
    private lateinit var btnMyComplaints: LinearLayout
    private lateinit var layoutComplaintStatus: LinearLayout  // ★ 새 카드

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnCreateComplaint = findViewById(R.id.btnCreateComplaint)
        btnMyComplaints = findViewById(R.id.btnMyComplaints)
        layoutComplaintStatus = findViewById(R.id.layoutComplaintStatus) // ★ XML에 있어야 함

        btnCreateComplaint.setOnClickListener {
            startActivity(Intent(this, CreateComplaintActivity::class.java))
        }

        btnMyComplaints.setOnClickListener {
            startActivity(Intent(this, MyComplaintsActivity::class.java))
        }

        layoutComplaintStatus.setOnClickListener {
            startActivity(Intent(this, ComplaintStatusActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadSummaryFromServer()
    }

    private fun loadSummaryFromServer() {
        lifecycleScope.launch {
            // 진행 중 / 완료 개수 불러오는 기존 코드
        }
    }
}
