package com.example.yonam_copspot.backend.repository

import com.example.yonam_copspot.backend.model.Complaint
import com.example.yonam_copspot.backend.model.ComplaintCreateRequest

/**
 * 아주 단순한 "메모리 기반" 구현체.
 *
 * - 앱이 켜져 있는 동안에는 데이터를 유지하지만,
 * - 앱을 완전히 종료했다가 다시 켜면 데이터가 사라진다.
 *
 * 대회용/프로토타입에는 충분하고,
 * 나중에 Room DB, 서버 연동 등으로 교체할 수 있다.
 */
class InMemoryComplaintRepository : ComplaintRepository {

    // 실제 데이터가 저장될 곳 (민원 목록)
    private val complaints = mutableListOf<Complaint>()

    override fun createComplaint(request: ComplaintCreateRequest): Complaint {
        // request(프런트에서 채워서 보낸 값)를 기반으로 Complaint 객체 만들기
        val newComplaint = Complaint(
            writerName = request.writerName,
            photoUri = request.photoUri,
            reportType = request.reportType,
            message = request.message,
            latitude = request.latitude,
            longitude = request.longitude,
            // isCompleted, createdAt, updatedAt, id는 기본값 사용
        )

        // 리스트에 추가
        complaints.add(newComplaint)

        // 프런트에 그대로 돌려주기
        return newComplaint
    }

    override fun getAllComplaints(): List<Complaint> {
        // 외부에서 리스트를 직접 수정하지 못하도록 복사본을 반환
        return complaints.toList()
    }

    override fun getComplaintById(id: String): Complaint? {
        return complaints.find { it.id == id }
    }

    override fun markComplaintCompleted(id: String, isCompleted: Boolean): Complaint? {
        val index = complaints.indexOfFirst { it.id == id }
        if (index == -1) {
            // 해당 ID가 없으면 null
            return null
        }

        val old = complaints[index]
        // data class는 copy(...)로 일부 필드만 수정 가능
        val updated = old.copy(
            isCompleted = isCompleted,
            updatedAt = System.currentTimeMillis()
        )

        complaints[index] = updated
        return updated
    }
}
