package com.example.yonam_copspot.backend.model

/**
 * "새 민원을 만들 때" 프런트(UI)가 백엔드 레이어로 넘겨주는 데이터 모양.
 *
 * UI 담당자는 이 클래스를 보고:
 *  - 사용자가 작성한 값들을 여기에 채워 넣고
 *  - ComplaintRepository.createComplaint(...) 에 넘기면 된다.
 *
 * ⚠️ 기본값(=TODO_...)은 "여기를 채워야 한다"는 표시용일 뿐,
 *    실제 앱에서는 기본값 그대로 쓰지 말고 꼭 값을 넣어서 사용해야 한다.
 */
data class ComplaintCreateRequest(
    val writerName: String =
        "TODO_프런트에서_작성자_이름을_여기에_채워야_합니다",

    val photoUri: String? =
        "TODO_프런트에서_사진_URI_또는_파일경로를_여기에_채워야_합니다",
    // 사진을 아직 안 다루겠다면 null 로 보내도 괜찮음.

    val reportType: String =
        "TODO_프런트에서_신고유형_문자열을_여기에_채워야_합니다",

    val message: String =
        "TODO_프런트에서_한마디_설명_문자열을_여기에_채워야_합니다",

    val latitude: Double? = null,
    val longitude: Double? = null
    // 위도/경도는 위치 권한 등 상황에 따라 null 일 수도 있음.
)
