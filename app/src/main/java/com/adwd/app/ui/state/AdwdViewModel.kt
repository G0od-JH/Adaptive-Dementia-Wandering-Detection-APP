package com.adwd.app.ui.state

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adwd.app.engine.AlertCallback
import com.adwd.app.engine.TrackPoint
import com.adwd.app.engine.WanderEngine
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.model.Place
import com.adwd.app.ui.model.SamplePlaces
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 화면이 경보를 받는 입구 (Observer 구독자 쪽).
 * C++ 엔진의 AlertListener → JNI → [AlertCallback] → 여기 onAlertChanged 순서로 들어온다.
 */
interface AlertUiListener {
    fun onAlertChanged(level: AlertLevel, reason: String)
}

enum class UserRole { GUARDIAN, ELDER }

/** 재생 속도. pointsPerSec = 1초에 넣는 모의데이터 줄 수 (1줄 = 1분) */
enum class SimSpeed(val label: String, val pointsPerSec: Int) { X1("1x", 1), X10("10x", 10), X60("60x", 60) }

/**
 * 데모 시나리오. file = assets(레포의 data/ 폴더)의 CSV.
 * TODO(코드A 금진): 이상 시나리오 3종 CSV를 data/ 에 이 이름으로 넣으면 바로 재생된다.
 * 파일이 아직 없으면 샘플 1일치로 대신 재생한다.
 */
enum class SimScenario(val label: String, val file: String) {
    NORMAL_WALK("정상 산책(먼 길)", "sample_day_2026-11-24.csv"),
    ROUTE_DEVIATION("경로 이탈", "scenario_route_deviation.csv"),
    NIGHT_OUTING("심야 외출", "scenario_night_outing.csv"),
    REPEATED_AREA("같은 구역 반복", "scenario_repeated_area.csv"),
}

const val FALLBACK_DATA_FILE = "sample_day_2026-11-24.csv"

/** 집 반경(m). 엔진 thresholds::kHomeRadiusM 와 같은 값 */
private const val HOME_RADIUS_M = 50.0

data class AdwdUiState(
    val role: UserRole = UserRole.GUARDIAN,
    val isLinked: Boolean = true,
    val level: AlertLevel = AlertLevel.NORMAL,
    val reason: String = "경로 이탈",
    // 11 시뮬레이션 데모 패널
    val scenario: SimScenario = SimScenario.NORMAL_WALK,
    val speed: SimSpeed = SimSpeed.X10,
    val isPlaying: Boolean = true,
    val simDay: Int = 1,
    val simTime: String = "00:00",
    // 지도 패널: 오늘 지나온 위치 + 현재 위치 (모의데이터 좌표, m, 집 = 0,0)
    val track: List<TrackPoint> = emptyList(),
    val current: TrackPoint? = null,
    /** 데모 패널 판단 로그 (최근 것이 위). 숫자 = 경보 단계 */
    val log: List<Pair<String, Int>> = emptyList(),
    /** "C++ 엔진 · 콜드 스타트" 같은 연결 상태 표시 */
    val engineStatus: String = "",
    /** 시나리오 CSV가 없어서 샘플 1일치로 대신 재생 중이면 true */
    val usingFallbackData: Boolean = false,
    // 04-1 자주 가는 장소 (콜드 스타트 대비, 보호자가 입력)
    val places: List<Place> = SamplePlaces.initial,
) {
    val home: Place? get() = places.firstOrNull { it.isHome }
}

class AdwdViewModel(app: Application) : AndroidViewModel(app), AlertUiListener {
    private val _state = MutableStateFlow(AdwdUiState())
    val state: StateFlow<AdwdUiState> = _state.asStateFlow()

    /** C++ 엔진이 단계를 바꿀 때 불리는 콜백 (JNI에서 호출, 재생 스레드) */
    private val engineCallback = object : AlertCallback {
        override fun onAlertChanged(from: Int, to: Int, reasons: Array<String>, x: Double, y: Double, time: String) {
            val level = AlertLevel.fromStep(to)
            val reason = reasons.firstOrNull() ?: _state.value.reason
            this@AdwdViewModel.onAlertChanged(level, reason)
            val hhmm = time.substring(11, 16)
            addLog("$hhmm ${AlertLevel.fromStep(from).label} → ${level.label}" + (reasons.joinToString(prefix = " · ").takeIf { reasons.isNotEmpty() } ?: ""), to)
        }
    }

    private var engine: WanderEngine? = null
    private var points: List<TrackPoint> = emptyList()
    private var index = 0
    private var playJob: Job? = null

    init {
        loadScenario(_state.value.scenario)
    }

    override fun onAlertChanged(level: AlertLevel, reason: String) {
        _state.update { it.copy(level = level, reason = reason) }
    }

    /** 3단계 긴급에서 보호자가 "괜찮아요"를 눌렀을 때 (보호자 수동 해제 → 엔진 resolve) */
    fun dismissAlert() {
        val e = engine
        if (e != null) {
            viewModelScope.launch(Dispatchers.Default) { synchronized(this@AdwdViewModel) { e.resolve() } }
        } else {
            onAlertChanged(AlertLevel.NORMAL, _state.value.reason)
        }
    }

    fun setRole(role: UserRole) = _state.update { it.copy(role = role) }
    fun setLinked(linked: Boolean) = _state.update { it.copy(isLinked = linked) }

    // 데모 패널 조작
    fun setScenario(s: SimScenario) {
        _state.update { it.copy(scenario = s, reason = s.label.substringBefore("(")) }
        loadScenario(s)
    }

    fun setSpeed(s: SimSpeed) = _state.update { it.copy(speed = s) }

    fun togglePlay() {
        _state.update { it.copy(isPlaying = !it.isPlaying) }
        when {
            !_state.value.isPlaying -> playJob?.cancel()
            index >= points.size -> loadScenario(_state.value.scenario)  // 하루가 끝났으면 처음부터
            else -> startPlayback()
        }
    }

    /** 데모 패널에서 단계 버튼을 직접 눌렀을 때 (발표용 수동 전환, 엔진 판단과 따로) */
    fun forceLevel(level: AlertLevel) {
        onAlertChanged(level, _state.value.reason)
        addLog("${_state.value.simTime} 수동 전환 → ${level.label}", level.step)
    }

    // ── 모의데이터 재생 → C++ 엔진 → 화면 ──

    private fun loadScenario(s: SimScenario) {
        playJob?.cancel()
        viewModelScope.launch {
            val (loaded, fallback) = withContext(Dispatchers.IO) {
                val assets = getApplication<Application>().assets
                val file = if (assets.list("")?.contains(s.file) == true) s.file else FALLBACK_DATA_FILE
                TrackPoint.parseCsv(assets.open(file).bufferedReader().use { it.readText() }) to (file != s.file)
            }
            synchronized(this@AdwdViewModel) {
                engine?.close()
                engine = if (WanderEngine.isAvailable) WanderEngine(engineCallback) else null
                points = loaded
                index = 0
            }
            val status = engine?.let { "C++ 엔진 연결됨 · ${it.strategyName}" } ?: "C++ 엔진 없음 (NDK 빌드 확인) · 지도만 재생"
            _state.update {
                it.copy(
                    level = AlertLevel.NORMAL, track = emptyList(), current = null, log = emptyList(),
                    simTime = loaded.firstOrNull()?.hhmm ?: "00:00", engineStatus = status, usingFallbackData = fallback,
                )
            }
            if (_state.value.isPlaying) startPlayback()
        }
    }

    private fun startPlayback() {
        playJob?.cancel()
        playJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val step = synchronized(this@AdwdViewModel) {
                    if (index >= points.size) null else {
                        val p = points[index++]
                        engine?.update(p)
                        p
                    }
                } ?: break
                val prev = _state.value.current
                if (prev != null && (prev.distanceFromHome <= HOME_RADIUS_M) != (step.distanceFromHome <= HOME_RADIUS_M)) {
                    addLog(if (step.distanceFromHome > HOME_RADIUS_M) "${step.hhmm} 외출 시작" else "${step.hhmm} 귀가 · 외출 기록 초기화", 0)
                }
                _state.update { st ->
                    st.copy(track = st.track + step, current = step, simTime = step.hhmm)
                }
                delay(1000L / _state.value.speed.pointsPerSec)
            }
            _state.update { it.copy(isPlaying = false) }
        }
    }

    private fun addLog(line: String, level: Int) =
        _state.update { it.copy(log = (listOf(line to level) + it.log).take(30)) }

    override fun onCleared() {
        playJob?.cancel()
        synchronized(this) { engine?.close(); engine = null }
    }

    // 04-1 / 04-2 / 08-1 장소 관리. TODO(민규): 저장소에 저장, 코드B 판단 엔진에 전달
    fun upsertPlace(place: Place) = _state.update { st ->
        val id = if (place.id < 0) (st.places.maxOfOrNull { it.id } ?: 0) + 1 else place.id
        val p = place.copy(id = id)
        // 집은 하나만: 다른 id의 집이 있으면 빼고, 같은 id는 그 자리에서 바꾼다
        val kept = st.places.filterNot { p.isHome && it.isHome && it.id != id }
        val replaced = if (kept.any { it.id == id }) kept.map { if (it.id == id) p else it } else kept + p
        st.copy(places = replaced.sortedByDescending { it.isHome })
    }

    fun removePlace(id: Int) = _state.update { st -> st.copy(places = st.places.filterNot { it.id == id && !it.isHome }) }
}
