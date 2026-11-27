package com.adwd.app.engine

/**
 * 모의데이터 한 줄 = 한 시점의 위치. 형식은 금진(코드A) 데이터 포맷 그대로.
 *   time,x,y
 *   2026-11-24T08:30:00,120.5,340.0
 * 좌표: 미터, 원점(0,0) = 어르신 집, x = 동쪽(+), y = 북쪽(+).
 *
 * 엔진에는 [raw] 줄을 그대로 넘긴다 (C++ LocationPoint::fromCsv가 다시 읽음).
 */
data class TrackPoint(val time: String, val x: Double, val y: Double, val raw: String) {
    /** "08:30" */
    val hhmm: String get() = time.substring(11, 16)

    val distanceFromHome: Double get() = kotlin.math.hypot(x, y)

    companion object {
        fun fromCsv(line: String): TrackPoint {
            val parts = line.trim().split(',')
            require(parts.size == 3) { "CSV 형식 오류: $line" }
            return TrackPoint(parts[0], parts[1].toDouble(), parts[2].toDouble(), line.trim())
        }

        /** 파일 전체. 첫 줄 헤더(time,x,y)와 빈 줄은 건너뛴다. */
        fun parseCsv(text: String): List<TrackPoint> =
            text.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("time") }
                .map(::fromCsv)
                .toList()
    }
}
