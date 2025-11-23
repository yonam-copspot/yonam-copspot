package com.example.yonam_copspot

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.yonam_copspot.network.RetrofitClient
import com.example.yonam_copspot.network.dto.CreateComplaintRequestDto
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.util.Log



class CreateComplaintActivity : AppCompatActivity() {

    // TODO: 실제 로그인 시스템 생기면 여기서 userId 받아오기
    private val loginUserId = "22360006"
    private val loginUserName = "김계영"

    // ★ 위치
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

    private lateinit var scrollView: ScrollView
    private lateinit var imagePreview: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var editWriterName: EditText
    private lateinit var editUserId: EditText
    private lateinit var editLocation: EditText
    private lateinit var editMessage: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button

    private var photoBase64: String? = null

    private val takePicturePreviewLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imagePreview.setImageBitmap(bitmap)
            photoBase64 = bitmap.toBase64()
        } else {
            Toast.makeText(this, "사진 촬영이 취소되었습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_complaint)

        supportActionBar?.title = "민원 작성"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        scrollView = findViewById(R.id.scrollViewCreateComplaint)
        imagePreview = findViewById(R.id.imagePreview)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        editWriterName = findViewById(R.id.editWriterName)
        editUserId = findViewById(R.id.editUserId)
        editLocation = findViewById(R.id.editLocation)
        editMessage = findViewById(R.id.editMessage)
        btnSubmit = findViewById(R.id.btnSubmitComplaint)
        btnCancel = findViewById(R.id.btnCancel)

        // 기본값으로 로그인 아이디/이름 넣어두기 (수정 가능)
        editUserId.setText(loginUserId)
        editWriterName.setText(loginUserName)

        btnTakePhoto.setOnClickListener {
            takePicturePreviewLauncher.launch(null)
        }

        btnSubmit.setOnClickListener {
            submitComplaintWithLocation()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        // 🔹 이거도 사실 없어도 됨 (원하면 두 둬도 상관 없지만 필수 X)
        // editMessage.setOnClickListener {
        //     scrollView.post {
        //         scrollView.smoothScrollTo(0, editMessage.bottom)
        //     }
        // }
    }

    private fun submitComplaintToServer(
        writerName: String,
        userId: String,
        locationText: String,
        description: String,
        latitude: Double?,
        longitude: Double?
    ) {
        val body = CreateComplaintRequestDto(
            userId = userId,
            authorName = writerName,
            photoBase64 = photoBase64,
            locationName = locationText,
            description = description,
            latitude = latitude,
            longitude = longitude
        )

        lifecycleScope.launch {
            try {
                RetrofitClient.api.createComplaint(body)
                Toast.makeText(this@CreateComplaintActivity, "민원이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CreateComplaintActivity, "민원 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitComplaintWithLocation() {
        val writerName = editWriterName.text.toString().trim()
        val userId = editUserId.text.toString().trim()
        val locationText = editLocation.text.toString().trim()
        val description = editMessage.text.toString().trim()

        if (writerName.isEmpty() || userId.isEmpty() || locationText.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "이름, 아이디, 장소, 설명을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // ★ 위치 권한 체크
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 권한 없으면 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            // onRequestPermissionsResult 에서 다시 submitComplaintWithLocation() 호출
            return
        }

        // ★ 권한이 있는 경우: 마지막 위치 가져오기
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->
                val lat = loc?.latitude
                val lng = loc?.longitude

                Log.d("CreateComplaintActivity", "현재 위치 위도: $lat, 경도: $lng")

                submitComplaintToServer(
                    writerName = writerName,
                    userId = userId,
                    locationText = locationText,
                    description = description,
                    latitude = lat,
                    longitude = lng
                )
            }
            .addOnFailureListener {
                Toast.makeText(this, "현재 위치를 가져오지 못했습니다. 위치 없이 등록합니다.", Toast.LENGTH_SHORT).show()

                submitComplaintToServer(
                    writerName = writerName,
                    userId = userId,
                    locationText = locationText,
                    description = description,
                    latitude = null,
                    longitude = null
                )
            }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한을 방금 허용했으니 다시 시도
                submitComplaintWithLocation()
            } else {
                // 거부한 경우: 위도/경도 없이 그냥 등록할지 여부 선택
                Toast.makeText(this, "위치 권한이 없어 위치 없이 민원을 등록합니다.", Toast.LENGTH_SHORT).show()

                val writerName = editWriterName.text.toString().trim()
                val userId = editUserId.text.toString().trim()
                val locationText = editLocation.text.toString().trim()
                val description = editMessage.text.toString().trim()

                if (writerName.isNotEmpty() && userId.isNotEmpty() && locationText.isNotEmpty() && description.isNotEmpty()) {
                    submitComplaintToServer(
                        writerName = writerName,
                        userId = userId,
                        locationText = locationText,
                        description = description,
                        latitude = null,
                        longitude = null
                    )
                }
            }
        }
    }



    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
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
