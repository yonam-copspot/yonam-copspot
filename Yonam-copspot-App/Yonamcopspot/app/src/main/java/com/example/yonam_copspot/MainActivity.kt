// app/src/main/java/com/example/yonam_copspot/MainActivity.kt
package com.example.yonam_copspot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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

    // actionbar 자동 완성을 위한 BaseActivity 클래스 생성 -> import 처리완료
    abstract class BaseActivity : AppCompatActivity() {

        protected fun applySystemInsets(
            root: View,
            toolbar: View? = null,
            content: View? = null
        ) {
            WindowCompat.setDecorFitsSystemWindows(window, false)

            ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                // 상태바 높이만큼 툴바 위 패딩
                toolbar?.updatePadding(top = bars.top)

                // 내비바 높이만큼 컨텐츠 아래 패딩
                content?.updatePadding(bottom = bars.bottom)

                insets
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadSummaryFromServer()
    }

    private fun loadSummaryFromServer() {
        lifecycleScope.launch {
            // TODO: 진행 중 / 완료 개수 불러오는 로직 구현
            // 예) val summary = RetrofitClient.api.getSummary(loginUserId)
            // textSummaryContent.text = ...
        }
    }
}
