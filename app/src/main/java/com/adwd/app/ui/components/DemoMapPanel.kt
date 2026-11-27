package com.adwd.app.ui.components

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.adwd.app.sim.MockDataRepository
import com.adwd.app.sim.MockScenario
import com.adwd.app.sim.TrackPoint
import com.adwd.app.sim.parseHhmm
import com.adwd.app.sim.splitOutings
import com.adwd.app.sim.until
import com.adwd.app.ui.state.SimScenario
import com.adwd.app.ui.theme.AdwdColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * 모의데이터를 읽어서 [MapPanel]에 넘기는 데모용 지도 (코드A · 금진).
 *
 * 데모 패널(11)에서 고른 시나리오의 외출을 그린다. 시뮬레이션 시계(코드A, 11/27~)가 붙기 전까지는
 * [simTime]이 외출 시간 안이면 그 시각까지, 아니면 시나리오마다 정해 둔 순간([MockScenario.snapshotMinute])까지 보여준다.
 * 데이터를 못 읽으면 기존 [MockMap] 그림으로 대신 보여준다.
 */
@Composable
fun DemoMapPanel(
    mode: MapPanelMode,
    scenario: SimScenario,
    modifier: Modifier = Modifier,
    accent: Color = AdwdColors.Level2,
    simTime: String? = null,
) {
    val context = LocalContext.current
    val data by produceState<DemoMapData?>(initialValue = null, scenario) {
        value = withContext(Dispatchers.IO) {
            try {
                DemoMapCache.load(MockDataRepository(context.applicationContext.assets), scenario.toMock())
            } catch (e: IOException) {
                Log.e("DemoMapPanel", "모의데이터를 못 읽음 (data/ 폴더가 assets에 잡혔는지 확인)", e)
                null
            } catch (e: IllegalArgumentException) {
                Log.e("DemoMapPanel", "모의데이터 형식 오류", e)
                null
            }
        }
    }

    val d = data
    if (d == null) {
        MockMap(mode.toVariant(), modifier, accent = accent)
        return
    }
    MapPanel(
        mode = mode,
        track = d.trackAt(simTime?.let(::parseHhmm)),
        usualRoutes = d.usualRoutes,
        accent = accent,
        modifier = modifier,
    )
}

class DemoMapData(
    val usualRoutes: List<List<TrackPoint>>,
    val outing: List<TrackPoint>,
    private val snapshotMinute: Int,
) {
    /** [minute]이 외출 중이면 그 시각까지, 아니면 정해 둔 순간까지 */
    fun trackAt(minute: Int?): List<TrackPoint> {
        if (outing.isEmpty()) return outing
        val inside = minute != null && minute in outing.first().minuteOfDay..outing.last().minuteOfDay
        return outing.until(if (inside) minute else snapshotMinute)
    }
}

/** 정상 30일은 시나리오가 바뀌어도 같으니 한 번만 읽는다 */
private object DemoMapCache {
    /** 평소 동선으로 그릴 최근 날짜 수 */
    private const val USUAL_DAYS = 14

    @Volatile private var usual: List<List<TrackPoint>>? = null

    fun load(repo: MockDataRepository, scenario: MockScenario): DemoMapData {
        val routes = usual ?: repo.normalDays().takeLast(USUAL_DAYS).flatMap { splitOutings(it) }.also { usual = it }
        return DemoMapData(routes, repo.scenarioOuting(scenario), scenario.snapshotMinute)
    }
}

private fun SimScenario.toMock(): MockScenario = when (this) {
    SimScenario.NORMAL_WALK -> MockScenario.NORMAL_LONG_WALK
    SimScenario.ROUTE_DEVIATION -> MockScenario.ROUTE_DEVIATION
    SimScenario.NIGHT_OUTING -> MockScenario.NIGHT_OUTING
    SimScenario.REPEATED_AREA -> MockScenario.REPETITIVE_LOOP
}

private fun MapPanelMode.toVariant(): MapVariant = when (this) {
    MapPanelMode.HOME -> MapVariant.HOME
    MapPanelMode.DEVIATION -> MapVariant.DEVIATION
    MapPanelMode.LAST_SEEN -> MapVariant.LAST_SEEN
}
