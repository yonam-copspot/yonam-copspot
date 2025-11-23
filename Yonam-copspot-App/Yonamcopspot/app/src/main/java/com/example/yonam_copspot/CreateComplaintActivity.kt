package com.example.yonam_copspot

<<<<<<< Updated upstream
import android.Manifest
import android.content.pm.PackageManager
=======
<<<<<<< HEAD
>>>>>>> Stashed changes
import android.graphics.Bitmap
import android.os.Bundle
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CreateComplaintActivity : AppCompatActivity() {

    private val loginUserId = "22360006"
    private val loginUserName = "김계영"

<<<<<<< Updated upstream
=======
    // ★ 위치
=======
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CreateComplaintActivity : AppCompatActivity() {

    private val loginUserId = "22360006"
    private val loginUserName = "김계영"

>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1001
    }

    private lateinit var scrollView: ScrollView
    private lateinit var imagePreview: ImageView
    private lateinit var btnTakePhoto: Button
    private lateinit var editWriterName: EditText
    private lateinit var editUserId: EditText
<<<<<<< Updated upstream
=======
<<<<<<< HEAD
    private lateinit var editLocation: EditText
=======
>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
    private lateinit var editMessage: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button

<<<<<<< Updated upstream
    // ✅ 장소 칩 관련
    private lateinit var chipGroupLocation: ChipGroup

=======
<<<<<<< HEAD
=======
    // ✅ 장소 칩 관련
    private lateinit var chipGroupLocation: ChipGroup

>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======
<<<<<<< HEAD
        editLocation = findViewById(R.id.editLocation)
=======
>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
        editMessage = findViewById(R.id.editMessage)
        btnSubmit = findViewById(R.id.btnSubmitComplaint)
        btnCancel = findViewById(R.id.btnCancel)

<<<<<<< Updated upstream
        // ✅ 칩/기타입력 뷰 연결
        chipGroupLocation = findViewById(R.id.chipGroupLocation)

        // 기본값으로 로그인 아이디/이름
        editUserId.setText(loginUserId)
        editWriterName.setText(loginUserName)


=======
<<<<<<< HEAD
        // 기본값으로 로그인 아이디/이름 넣어두기 (수정 가능)
        editUserId.setText(loginUserId)
        editWriterName.setText(loginUserName)

=======
        // ✅ 칩/기타입력 뷰 연결
        chipGroupLocation = findViewById(R.id.chipGroupLocation)

        // 기본값으로 로그인 아이디/이름
        editUserId.setText(loginUserId)
        editWriterName.setText(loginUserName)


>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
        btnTakePhoto.setOnClickListener {
            takePicturePreviewLauncher.launch(null)
        }

        btnSubmit.setOnClickListener {
            submitComplaintWithLocation()
        }

        btnCancel.setOnClickListener {
            finish()
        }
<<<<<<< Updated upstream
    }

=======
<<<<<<< HEAD

        // 🔹 이거도 사실 없어도 됨 (원하면 두 둬도 상관 없지만 필수 X)
        // editMessage.setOnClickListener {
        //     scrollView.post {
        //         scrollView.smoothScrollTo(0, editMessage.bottom)
        //     }
        // }
    }

=======
    }

>>>>>>> Stashed changes
    // ✅ 칩에서 장소 가져오기
    private fun getSelectedLocation(): String {
        val checkedId = chipGroupLocation.checkedChipId
        if (checkedId == View.NO_ID) return ""
        return chipGroupLocation.findViewById<Chip>(checkedId)
            .text.toString()
            .trim()
    }

//  기타로 저장되어서 넘어가게 하려면 이렇게
//    return if (selected == "기타") {
//        editEtcLocation.text?.toString()?.trim()
//            .takeUnless { it.isNullOrEmpty() } ?: "기타"
//    } else selected


<<<<<<< Updated upstream
=======
>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
        val locationText = getSelectedLocation()
=======
<<<<<<< HEAD
        val locationText = editLocation.text.toString().trim()
>>>>>>> Stashed changes
        val description = editMessage.text.toString().trim()

        if (writerName.isEmpty() || userId.isEmpty() || locationText.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "이름, 학번, 장소, 설명을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

<<<<<<< Updated upstream
        // 위치 권한 체크
=======
        // ★ 위치 권한 체크
=======
        val locationText = getSelectedLocation()
        val description = editMessage.text.toString().trim()

        if (writerName.isEmpty() || userId.isEmpty() || locationText.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "이름, 학번, 장소, 설명을 모두 입력해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 위치 권한 체크
>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
<<<<<<< Updated upstream
=======
<<<<<<< HEAD
            // 권한 없으면 요청
=======
>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
<<<<<<< Updated upstream
            return
        }

=======
<<<<<<< HEAD
            // onRequestPermissionsResult 에서 다시 submitComplaintWithLocation() 호출
            return
        }

        // ★ 권한이 있는 경우: 마지막 위치 가져오기
=======
            return
        }

>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream

=======
<<<<<<< HEAD
=======

>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
<<<<<<< Updated upstream
                submitComplaintWithLocation()
            } else {
=======
<<<<<<< HEAD
                // 권한을 방금 허용했으니 다시 시도
                submitComplaintWithLocation()
            } else {
                // 거부한 경우: 위도/경도 없이 그냥 등록할지 여부 선택
=======
                submitComplaintWithLocation()
            } else {
>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
                Toast.makeText(this, "위치 권한이 없어 위치 없이 민원을 등록합니다.", Toast.LENGTH_SHORT).show()

                val writerName = editWriterName.text.toString().trim()
                val userId = editUserId.text.toString().trim()
<<<<<<< Updated upstream
                val locationText = getSelectedLocation()
                val description = editMessage.text.toString().trim()

                if (writerName.isNotEmpty() && userId.isNotEmpty() &&
                    locationText.isNotEmpty() && description.isNotEmpty()
                ) {
=======
<<<<<<< HEAD
                val locationText = editLocation.text.toString().trim()
                val description = editMessage.text.toString().trim()

                if (writerName.isNotEmpty() && userId.isNotEmpty() && locationText.isNotEmpty() && description.isNotEmpty()) {
=======
                val locationText = getSelectedLocation()
                val description = editMessage.text.toString().trim()

                if (writerName.isNotEmpty() && userId.isNotEmpty() &&
                    locationText.isNotEmpty() && description.isNotEmpty()
                ) {
>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
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

<<<<<<< Updated upstream
=======
<<<<<<< HEAD


=======
>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======
<<<<<<< HEAD

=======
>>>>>>> copspot-android-frontend
>>>>>>> Stashed changes
            else -> super.onOptionsItemSelected(item)
        }
    }
}
