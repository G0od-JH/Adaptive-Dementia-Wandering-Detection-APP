package com.adwd.app.sim

/**
 * `data/scenario/` 파일과 1:1. 데모 패널(11)의 SimScenario와 같은 순서.
 *
 * @param focusStartMinute 데모로 보여줄 외출이 시작하는 무렵 (이 시각 이후 끝나는 첫 외출)
 * @param snapshotMinute 시뮬레이션 시계가 없을 때 지도에 보여줄 "그 순간".
 *   외출 전체를 그리면 다 집으로 돌아온 뒤라 현재 위치가 집에 찍히므로, 가장 멀리 있는 무렵을 고름
 */
enum class MockScenario(val fileName: String, val focusStartMinute: Int, val snapshotMinute: Int) {
    /** 10:05 출발, 10:24~10:39 경로당 */
    NORMAL_LONG_WALK("normal_long_walk_2026-10-31.csv", 10 * 60, 10 * 60 + 30),
    /** 07:05 출발, 07:45쯤 1.4km 밖, 이후 헤매다 09:16 귀가. 07:55면 이상 지속 30분 넘어 위험 단계 */
    ROUTE_DEVIATION("route_deviation_2026-10-31.csv", 7 * 60, 7 * 60 + 55),
    /** 02:10 출발, 02:23~02:48 공원 */
    NIGHT_OUTING("night_outing_2026-10-31.csv", 2 * 60, 2 * 60 + 30),
    /** 14:13쯤부터 공원 블록을 반복해서 돎 */
    REPETITIVE_LOOP("repetitive_loop_2026-10-31.csv", 14 * 60, 15 * 60 + 30),
}

/** 모의 동네의 자주 가는 곳 (tools/gen_mock.py 좌표와 같음) */
data class Landmark(val name: String, val x: Double, val y: Double)

object MockLandmarks {
    val park = Landmark("공원", 400.0, 250.0)
    val seniorCenter = Landmark("경로당", -350.0, 600.0)
    val mart = Landmark("마트", 200.0, -300.0)
    val all = listOf(park, seniorCenter, mart)

    /** 공원 영역 (m). 같은 구역 반복 시나리오가 이 블록을 돈다 */
    const val PARK_W = 100.0
    const val PARK_H = 100.0
}
