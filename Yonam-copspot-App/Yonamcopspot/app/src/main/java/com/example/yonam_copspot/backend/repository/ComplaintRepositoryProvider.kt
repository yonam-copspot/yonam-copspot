package com.example.yonam_copspot.backend.repository

/**
 * 앱 전체에서 하나만 사용하는 ComplaintRepository 싱글톤.
 *
 * 지금은 InMemoryComplaintRepository 를 쓰고,
 * 나중에 서버/DB 연동이 준비되면 여기에서 구현체만 교체하면 된다.
 */
object ComplaintRepositoryProvider {

    val repository: ComplaintRepository by lazy {
        InMemoryComplaintRepository()
        // --- 나중에 DB 붙일 때는 예를 들어 ---
        // RemoteComplaintRepository(retrofitService)
    }
}
