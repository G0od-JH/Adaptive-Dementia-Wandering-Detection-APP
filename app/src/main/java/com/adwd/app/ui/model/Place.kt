package com.adwd.app.ui.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.adwd.app.ui.components.AdwdIcons

/**
 * 보호자가 처음에 적는 어르신의 집·자주 가는 장소 (콜드 스타트 대비).
 * 평소 동선을 배우는 처음 30일 동안 코드B가 이 장소들을 기준으로 판단한다.
 * TODO(민규 데이터 모델 / 코드B): 확정된 Place 모델로 교체
 */
enum class PlaceType(val label: String, val icon: ImageVector) {
    HOME("집", AdwdIcons.Home),
    SENIOR_CENTER("경로당", AdwdIcons.Person),
    HOSPITAL("병원", AdwdIcons.Hospital),
    MARKET("시장", AdwdIcons.Market),
    PARK("공원", AdwdIcons.Tree),
    ETC("기타", AdwdIcons.Pin),
}

enum class VisitTime(val label: String) { MORNING("오전"), AFTERNOON("오후"), EVENING("저녁") }

data class Place(
    val id: Int,
    val name: String,
    val type: PlaceType,
    /** 모의 지도 위 위치 (0~1 비율). 실제 지도가 오면 위경도로 바꾼다. */
    val x: Float,
    val y: Float,
    val times: Set<VisitTime> = emptySet(),
) {
    val isHome: Boolean get() = type == PlaceType.HOME

    /** 모의 주소 표기 */
    val addressLabel: String get() = "모의 좌표 (${(x * 100).toInt()}, ${(y * 100).toInt()})"
}

object SamplePlaces {
    val initial = listOf(
        Place(1, "집", PlaceType.HOME, 0.14f, 0.79f),
        Place(2, "○○경로당", PlaceType.SENIOR_CENTER, 0.45f, 0.46f, setOf(VisitTime.AFTERNOON)),
    )
}
