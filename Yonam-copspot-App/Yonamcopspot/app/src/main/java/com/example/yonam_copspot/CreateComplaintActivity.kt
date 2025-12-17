package com.example.yonam_copspot

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.yonam_copspot.network.RetrofitClient
import com.example.yonam_copspot.network.dto.CreateComplaintRequestDto
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CreateComplaintActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

    private lateinit var imagePreview: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var editWriterName: EditText
    private lateinit var editUserId: EditText
    private lateinit var chipGroupLocation: ChipGroup
    private lateinit var editMessage: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button

    private var photoBase64: String? = null

    private val takePicturePreviewLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
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

        imagePreview = findViewById(R.id.imagePreview)
        btnTakePhoto = findViewById(R.id.btnTakePhoto)
        editWriterName = findViewById(R.id.editWriterName)
        editUserId = findViewById(R.id.editUserId)
        chipGroupLocation = findViewById(R.id.chipGroupLocation)
        editMessage = findViewById(R.id.editMessage)
        btnSubmit = findViewById(R.id.btnSubmitComplaint)
        btnCancel = findViewById(R.id.btnCancel)

        // ✅ LoginSession 값으로 기본 세팅
        editUserId.setText(LoginSession.userId)
        editWriterName.setText(LoginSession.userName)

        btnTakePhoto.setOnClickListener {
            takePicturePreviewLauncher.launch(null)
        }

        btnSubmit.setOnClickListener {
            submitComplaintWithLocation()
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun getSelectedLocation(): String {
        val checkedId = chipGroupLocation.checkedChipId
        if (checkedId == View.NO_ID) return ""
        return chipGroupLocation.findViewById<Chip>(checkedId).text.toString().trim()
    }

    private fun submitComplaintWithLocation() {
        val writerName = editWriterName.text.toString().trim()
        val userId = editUserId.text.toString().trim()
        val locationText = getSelectedLocation()
        val description = editMessage.text.toString().trim()

        if (writerName.isEmpty() || userId.isEmpty() || locationText.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "이름, 학번, 장소, 설명을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 입력값을 LoginSession에 반영
        LoginSession.userId = userId
        LoginSession.userName = writerName

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // ✅ 중복 클릭 방지(선택)
        btnSubmit.isEnabled = false

        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->
                submitComplaintToServer(
                    latitude = loc?.latitude,
                    longitude = loc?.longitude,
                    locationText = locationText,
                    description = description
                )
            }
            .addOnFailureListener {
                submitComplaintToServer(
                    latitude = null,
                    longitude = null,
                    locationText = locationText,
                    description = description
                )
            }
    }

    private fun submitComplaintToServer(
        latitude: Double?,
        longitude: Double?,
        locationText: String,
        description: String
    ) {
        val body = CreateComplaintRequestDto(
            userId = LoginSession.userId,
            authorName = LoginSession.userName,
            photoBase64 = photoBase64,
            locationName = locationText,
            description = description,
            latitude = latitude,
            longitude = longitude
        )

        lifecycleScope.launch {
            try {
                RetrofitClient.api.createComplaint(body)

                // ✅ 성공: 토스트/즉시 종료 대신 효과 + 사운드
                showCompleteEffectAndFinish()

            } catch (e: Exception) {
                Log.e("CreateComplaint", "등록 실패", e)
                Toast.makeText(this@CreateComplaintActivity, "민원 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()

                // ✅ 실패: 다시 누를 수 있게
                btnSubmit.isEnabled = true
            }
        }
    }

    // 사운드 + 성공효과 12/18 추가
    private fun showCompleteEffectAndFinish() {
        val overlay = findViewById<FrameLayout>(R.id.completeOverlay)
        overlay.visibility = View.VISIBLE

        // res/raw/complete_sound.mp3 → R.raw.complete_sound
        val mp = MediaPlayer.create(this, R.raw.complete_sound)
        mp.start()

        Handler(Looper.getMainLooper()).postDelayed({
            overlay.visibility = View.GONE
            mp.release()
            finish() // 이전 화면(목록)으로 돌아감
        }, 1200)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            submitComplaintWithLocation()
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else super.onOptionsItemSelected(item)
    }
}
