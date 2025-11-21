package com.example.yonam_copspot.backend.repository

import com.example.yonam_copspot.backend.model.Complaint
import com.example.yonam_copspot.backend.model.ComplaintCreateRequest

/**
 * 민원 데이터를 다루는 "창구" 역할 인터페이스.
 *
 * UI(프런트)는 이 인터페이스만 보고:
 *  - 민원 새로 만들기
 *  - 민원 목록 조회
 *  - 특정 민원 조회
 *  - 처리완료 상태 변경
 * 을 요청할 수 있다.
 *
 * 지금은 구현체로 "InMemoryComplaintRepository" 를 만들겠지만,
 * 나중에 Room, 서버 연동 등으로 갈아끼우기 쉽도록 인터페이스로 분리했다.
 */
interface ComplaintRepository {

    /**
     * 새 민원을 생성한다.
     *
     * @param request 프런트에서 채워서 넘겨주는 값(작성자, 신고유형, 한마디, 좌표, 사진 등)
     * @return 실제로 저장된 Complaint (id, 시간, 초기 상태까지 포함)
     */
    fun createComplaint(request: ComplaintCreateRequest): Complaint

    /**
     * 전체 민원 목록을 가져온다.
     * 지도/리스트 화면에서 사용.
     */
    fun getAllComplaints(): List<Complaint>

    /**
     * ID로 특정 민원을 찾는다.
     * 없으면 null 반환.
     */
    fun getComplaintById(id: String): Complaint?

    /**
     * 처리완료 버튼을 눌렀을 때 상태 변경.
     *
     * @param id         상태를 바꾸고 싶은 민원 ID
     * @param isCompleted true 이면 처리완료, false 이면 미완료로 되돌림
     * @return 변경된 Complaint (없으면 null)
     */
    fun markComplaintCompleted(id: String, isCompleted: Boolean = true): Complaint?
}
