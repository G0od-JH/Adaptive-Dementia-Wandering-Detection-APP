package com.adwd.app.ui.state

import androidx.lifecycle.ViewModel
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.model.Place
import com.adwd.app.ui.model.SamplePlaces
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 화면이 경보를 받는 입구 (Observer 구독자 쪽).
 *
 * 코드B가 정한 AlertListener 인터페이스가 확정되면, 그 인터페이스를 이 ViewModel이 구현하도록 바꾸고
 * onAlertChanged 안에서 AlertState → [AlertLevel.fromStep] 으로 변환만 하면 된다.
 */
interface AlertUiListener {
    fun onAlertChanged(level: AlertLevel, reason: String)
}

enum class UserRole { GUARDIAN, ELDER }

enum class SimSpeed(val label: String) { X1("1x"), X10("10x"), X60("60x") }

enum class SimScenario(val label: String) {
    NORMAL_WALK("정상 산책(먼 길)"),
    ROUTE_DEVIATION("경로 이탈"),
    NIGHT_OUTING("심야 외출"),
    REPEATED_AREA("같은 구역 반복"),
}

data class AdwdUiState(
    val role: UserRole = UserRole.GUARDIAN,
    val isLinked: Boolean = true,
    val level: AlertLevel = AlertLevel.NORMAL,
    val reason: String = "경로 이탈",
    // 11 시뮬레이션 데모 패널
    val scenario: SimScenario = SimScenario.ROUTE_DEVIATION,
    val speed: SimSpeed = SimSpeed.X10,
    val isPlaying: Boolean = true,
    val simDay: Int = 31,
    val simTime: String = "14:32",
    // 04-1 자주 가는 장소 (콜드 스타트 대비, 보호자가 입력)
    val places: List<Place> = SamplePlaces.initial,
) {
    val home: Place? get() = places.firstOrNull { it.isHome }
}

class AdwdViewModel : ViewModel(), AlertUiListener {
    private val _state = MutableStateFlow(AdwdUiState())
    val state: StateFlow<AdwdUiState> = _state.asStateFlow()

    override fun onAlertChanged(level: AlertLevel, reason: String) {
        _state.update { it.copy(level = level, reason = reason) }
    }

    /** 어르신이 "괜찮아요, 산책 중이에요"를 누르거나, 보호자가 "알고 있는 외출"로 처리했을 때 */
    fun dismissAlert() = onAlertChanged(AlertLevel.NORMAL, _state.value.reason)

    fun setRole(role: UserRole) = _state.update { it.copy(role = role) }
    fun setLinked(linked: Boolean) = _state.update { it.copy(isLinked = linked) }

    // 데모 패널 조작
    fun setScenario(s: SimScenario) = _state.update { it.copy(scenario = s, reason = s.label.substringBefore("(")) }
    fun setSpeed(s: SimSpeed) = _state.update { it.copy(speed = s) }
    fun togglePlay() = _state.update { it.copy(isPlaying = !it.isPlaying) }

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
