package com.adwd.app.ui.model

/**
 * 화면을 먼저 만들기 위한 가짜 데이터.
 * 코드A(모의데이터·위치 기록)와 민규(User 모델)의 실제 데이터가 오면 이 파일 대신 그쪽을 쓴다.
 */
object FakeData {
    const val elderId = "elder_kim"
    const val guardianId = "guardian01"
    const val linkCode = "482173"

    /** "482 173" 처럼 3자리씩 띄운 표기 */
    val linkCodeSpaced: String get() = linkCode.chunked(3).joinToString(" ")

    data class AlertRecord(
        val id: Int,
        val level: AlertLevel,
        val reason: String,
        val detail: String,
        val dateLabel: String,
    )

    val alertRecords = listOf(
        AlertRecord(1, AlertLevel.WARNING, "경로 이탈", "14:32 · 15:05 귀가하여 해제", "오늘 · 11월 24일"),
        AlertRecord(2, AlertLevel.CAUTION, "경로 이탈", "14:28 · 2단계로 올라감", "오늘 · 11월 24일"),
        AlertRecord(3, AlertLevel.CAUTION, "심야 외출", "23:40 · 알고 있는 외출로 처리", "11월 22일"),
        AlertRecord(4, AlertLevel.CAUTION, "같은 구역 반복", "16:10 · 평소 동선으로 돌아와 해제", "11월 19일"),
    )

    /** 판단 이유 한 줄. kind로 아이콘을 고른다. */
    enum class ReasonKind { DISTANCE, DURATION, TIME_OF_DAY, REPEAT }

    data class Reason(val kind: ReasonKind, val label: String, val value: String, val exceeded: Boolean)

    data class TimelineItem(val time: String, val level: AlertLevel, val text: String)

    /** 06 경보 상세 한 건. resolved가 있으면 이미 끝난 기록이다. */
    data class AlertDetail(
        val level: AlertLevel,
        val reason: String,
        val reasons: List<Reason>,
        val timeline: List<TimelineItem>,
        val resolved: String?,
    )

    private val routeWarningReasons = listOf(
        Reason(ReasonKind.DISTANCE, "평소 동선에서 거리", "420m / 기준 300m", true),
        Reason(ReasonKind.DURATION, "벗어난 시간", "12분 / 기준 10분", true),
        Reason(ReasonKind.TIME_OF_DAY, "외출 시간대", "평소 범위", false),
    )
    private val routeWarningTimeline = listOf(
        TimelineItem("14:20", AlertLevel.NORMAL, "산책 시작"),
        TimelineItem("14:28", AlertLevel.CAUTION, "어르신께 귀가 안내 보냄"),
        TimelineItem("14:32", AlertLevel.WARNING, "보호자 알림"),
    )

    /**
     * 기록 id → 상세. id 0은 "지금 울리고 있는 경보"(보호자 홈에서 들어올 때).
     * TODO(코드B): 판단 엔진이 남긴 기록으로 교체
     */
    val alertDetails: Map<Int, AlertDetail> = mapOf(
        0 to AlertDetail(AlertLevel.WARNING, "경로 이탈", routeWarningReasons, routeWarningTimeline, resolved = null),
        1 to AlertDetail(AlertLevel.WARNING, "경로 이탈", routeWarningReasons, routeWarningTimeline + TimelineItem("15:05", AlertLevel.NORMAL, "귀가하여 해제"), resolved = "15:05 귀가하여 해제"),
        2 to AlertDetail(
            AlertLevel.CAUTION, "경로 이탈",
            listOf(
                Reason(ReasonKind.DISTANCE, "평소 동선에서 거리", "310m / 기준 300m", true),
                Reason(ReasonKind.DURATION, "벗어난 시간", "4분 / 기준 10분", false),
                Reason(ReasonKind.TIME_OF_DAY, "외출 시간대", "평소 범위", false),
            ),
            listOf(
                TimelineItem("14:20", AlertLevel.NORMAL, "산책 시작"),
                TimelineItem("14:28", AlertLevel.CAUTION, "어르신께 귀가 안내 보냄"),
            ),
            resolved = "14:32 2단계 경고로 올라감",
        ),
        3 to AlertDetail(
            AlertLevel.CAUTION, "심야 외출",
            listOf(
                Reason(ReasonKind.TIME_OF_DAY, "외출 시간대", "23:40 / 평소 06~21시", true),
                Reason(ReasonKind.DISTANCE, "평소 동선에서 거리", "80m / 기준 300m", false),
                Reason(ReasonKind.DURATION, "밖에 있던 시간", "12분", false),
            ),
            listOf(
                TimelineItem("23:40", AlertLevel.CAUTION, "어르신께 귀가 안내 보냄"),
                TimelineItem("23:52", AlertLevel.NORMAL, "알고 있는 외출로 처리"),
            ),
            resolved = "보호자가 알고 있는 외출로 처리",
        ),
        4 to AlertDetail(
            AlertLevel.CAUTION, "같은 구역 반복",
            listOf(
                Reason(ReasonKind.REPEAT, "같은 구역 반복", "4바퀴 / 기준 3바퀴", true),
                Reason(ReasonKind.DISTANCE, "평소 동선에서 거리", "150m / 기준 300m", false),
                Reason(ReasonKind.TIME_OF_DAY, "외출 시간대", "평소 범위", false),
            ),
            listOf(
                TimelineItem("15:50", AlertLevel.NORMAL, "산책 시작"),
                TimelineItem("16:05", AlertLevel.CAUTION, "어르신께 귀가 안내 보냄"),
                TimelineItem("16:10", AlertLevel.NORMAL, "평소 동선으로 돌아와 해제"),
            ),
            resolved = "평소 동선으로 돌아와 해제",
        ),
    )

    /** 07 실종 신고 정보의 최근 위치 기록 */
    val recentPlaces = listOf(
        "14:58" to "버스정류장 부근",
        "14:40" to "공원 동쪽 출입구",
        "14:20" to "집 출발",
    )
}
