package com.example.yonam_copspot.backend.model

import java.util.UUID

/**
 * 앱 안에서 사용할 "민원 한 건"의 데이터 모양.
 *
 * UI(프런트)는 이 클래스를 보고:
 *  - 리스트에 뿌리거나
 *  - 지도에 핀 찍거나
 *  - 상세 화면을 보여줄 수 있다.
 *
 * ⚠️ 여기 들어가는 값들 중 대부분은
 *    "프런트(UI)에서 입력받아서 채워야 하는 값"이기 때문에
 *    아래에 TODO_... 라는 문자열로 표시해두었다.
 *    (실제 앱에서는 프런트에서 진짜 값으로 바꾸어 넣어야 한다.)
 */
data class Complaint(
    val id: String = UUID.randomUUID().toString(),
    // ↑ 각 민원을 구분하기 위한 고유 ID.
    //   기본값은 랜덤 UUID. 특별히 건드릴 필요는 없음.

    val writerName: String =
        "TODO_프런트에서_작성자_이름을_넣어야_합니다",
    // ↑ 작성자 이름.
    //   예: "홍길동"

    val photoUri: String? =
        "TODO_프런트에서_사진_URI_또는_파일경로를_넣을_예정입니다",
    // ↑ 사진을 가리키는 URI 또는 파일 경로를 문자열로 저장.
    //   예: content://... 또는 /storage/.../image.jpg
    //   아직 사진 기능 안 붙였으면 null로 두어도 된다.

    val reportType: String =
        "TODO_프런트에서_신고유형_문자열을_넣어야_합니다",
    // ↑ 신고유형.
    //   예: "포트홀", "불법주차", "쓰레기", "기타" 등
    //   나중에 enum으로 빼도 된다.

    val message: String =
        "TODO_프런트에서_한마디_설명을_넣어야_합니다",
    // ↑ 사용자가 간단히 적는 설명(한마디).
    //   예: "운동장 옆 도로에 큰 구멍이 있어요."

    val isCompleted: Boolean = false,
    // ↑ 처리완료 여부.
    //   - 처음 등록할 때는 기본적으로 false
    //   - 관리자가 "처리완료" 버튼을 누르면 true 로 변경.

    val latitude: Double? = null,
    val longitude: Double? = null,
    // ↑ GPS 좌표.
    //   - latitude : 위도 (Y)
    //   - longitude: 경도 (X)
    //   프런트에서 FusedLocationProvider 등으로 값을 받아서 넣어줘야 한다.

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
    // ↑ 생성 시각, 마지막 수정 시각 (밀리초 단위)
    //   - 리스트 정렬(최신 순) 등에 활용 가능.
)
