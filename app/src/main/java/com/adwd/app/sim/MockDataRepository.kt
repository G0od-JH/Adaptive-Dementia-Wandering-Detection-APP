package com.adwd.app.sim

import android.content.res.AssetManager

/**
 * 저장소 루트 `data/` 폴더의 모의데이터를 앱 assets에서 읽는다 (코드A · 금진).
 * `app/build.gradle.kts`에서 `data/`를 assets 폴더로 잡아 두었다.
 */
class MockDataRepository(private val assets: AssetManager) {

    /** 정상 생활 30일. 날짜순, 하루 = 위치 1440개 */
    fun normalDays(): List<List<TrackPoint>> =
        (assets.list(NORMAL_DIR) ?: emptyArray())
            .filter { it.endsWith(".csv") }
            .sorted()
            .map { read("$NORMAL_DIR/$it") }

    /** 시나리오 하루치 */
    fun scenarioDay(scenario: MockScenario): List<TrackPoint> = read("$SCENARIO_DIR/${scenario.fileName}")

    /** 시나리오에서 데모로 보여줄 외출 하나 (시나리오마다 정해 둔 시작 시각의 외출) */
    fun scenarioOuting(scenario: MockScenario): List<TrackPoint> {
        val outings = splitOutings(scenarioDay(scenario))
        return outings.firstOrNull { it.last().minuteOfDay >= scenario.focusStartMinute } ?: outings.lastOrNull().orEmpty()
    }

    private fun read(path: String): List<TrackPoint> =
        assets.open(path).bufferedReader(Charsets.UTF_8).useLines { MockTrackParser.parse(it) }

    companion object {
        const val NORMAL_DIR = "normal"
        const val SCENARIO_DIR = "scenario"
    }
}
