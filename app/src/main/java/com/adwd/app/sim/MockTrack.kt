package com.adwd.app.sim

import kotlin.math.hypot

/**
 * 모의데이터 CSV 한 줄 = 위치 1개 (코드A · 금진).
 *
 * 좌표는 m 단위 격자, 원점(0, 0) = 어르신 집, x = 동쪽(+), y = 북쪽(+).
 * 포맷 자세한 설명은 저장소 루트의 `data/FORMAT.md`.
 */
data class TrackPoint(
    /** "2026-10-31T07:05:00" */
    val time: String,
    val x: Double,
    val y: Double,
) {
    /** 하루 중 몇 번째 분인지 (00:00 = 0, 23:59 = 1439) */
    val minuteOfDay: Int get() = time.substring(11, 13).toInt() * 60 + time.substring(14, 16).toInt()

    /** "07:05" */
    val hhmm: String get() = time.substring(11, 16)

    val distanceFromHome: Double get() = hypot(x, y)
}

object MockTrackParser {
    const val HEADER = "time,x,y"

    /** CSV 전체(헤더 포함 가능) → 위치 목록. 빈 줄은 건너뛴다. */
    fun parse(lines: Sequence<String>): List<TrackPoint> =
        lines.map { it.trim() }
            .filter { it.isNotEmpty() && it != HEADER }
            .map(::parseLine)
            .toList()

    /** 데이터 한 줄 → 위치. 형식이 틀리면 IllegalArgumentException (원인 포함). */
    fun parseLine(line: String): TrackPoint {
        val parts = line.split(',')
        require(parts.size == 3) { "모의데이터 형식 오류(열 3개 아님): $line" }
        val time = parts[0]
        require(time.length >= 16 && time[10] == 'T' && time[13] == ':') { "모의데이터 시각 형식 오류: $line" }
        val x = parts[1].toDoubleOrNull() ?: throw IllegalArgumentException("모의데이터 x 오류: $line")
        val y = parts[2].toDoubleOrNull() ?: throw IllegalArgumentException("모의데이터 y 오류: $line")
        return TrackPoint(time, x, y)
    }
}

/**
 * 하루 기록을 외출 단위로 자른다.
 *
 * 집 반경 밖으로 나간 연속 구간 하나 = 외출 하나. 지도에 선이 집에서 시작해서 집에서 끝나도록
 * 구간 앞뒤로 집 안쪽 점을 하나씩 붙인다. 반경은 C++ `thresholds::kHomeRadiusM`(50m)과 같은 값.
 */
fun splitOutings(day: List<TrackPoint>, homeRadiusM: Double = HOME_RADIUS_M): List<List<TrackPoint>> {
    val outings = mutableListOf<List<TrackPoint>>()
    var start = -1
    for (i in day.indices) {
        val outside = day[i].distanceFromHome > homeRadiusM
        if (outside && start < 0) start = i
        if (!outside && start >= 0) {
            outings += day.subList(maxOf(0, start - 1), i + 1).toList()
            start = -1
        }
    }
    if (start >= 0) outings += day.subList(maxOf(0, start - 1), day.size).toList()
    return outings
}

/** 외출 중 시각 [minuteOfDay]까지 지나온 부분. 그 시각이 외출 범위 밖이면 외출 전체. */
fun List<TrackPoint>.until(minuteOfDay: Int?): List<TrackPoint> {
    if (minuteOfDay == null || isEmpty()) return this
    if (minuteOfDay < first().minuteOfDay || minuteOfDay > last().minuteOfDay) return this
    return takeWhile { it.minuteOfDay <= minuteOfDay }
}

/** "14:32" → 872. 형식이 틀리면 null */
fun parseHhmm(hhmm: String): Int? {
    val parts = hhmm.split(':')
    if (parts.size != 2) return null
    val h = parts[0].toIntOrNull() ?: return null
    val m = parts[1].toIntOrNull() ?: return null
    return if (h in 0..23 && m in 0..59) h * 60 + m else null
}

const val HOME_RADIUS_M = 50.0
