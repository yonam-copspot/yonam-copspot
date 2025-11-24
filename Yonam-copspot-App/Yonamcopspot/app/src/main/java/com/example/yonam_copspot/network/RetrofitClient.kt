// app/src/main/java/com/example/yonam_copspot/network/RetrofitClient.kt
package com.example.yonam_copspot.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 여기가 포인트: 사용자 말대로 이 주소를 베이스로 사용
    private const val BASE_URL = "http://182.31.216.234:3000/"

    val api: CopspotApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(CopspotApi::class.java)
    }
}
