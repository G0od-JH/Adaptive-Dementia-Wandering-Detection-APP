package com.adwd.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.sim.Landmark
import com.adwd.app.sim.MockLandmarks
import com.adwd.app.sim.TrackPoint
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.Pretendard
import kotlin.math.max
import kotlin.math.min

/** 지도 패널을 쓰는 화면. MockMap의 MapVariant와 같은 3가지 */
enum class MapPanelMode {
    /** 05 보호자 홈: 학습된 동선 + 지금 외출 경로 + 현재 위치 */
    HOME,
    /** 06 경보 상세: 평소 동선 + 벗어난 경로(경보 색 점선) */
    DEVIATION,
    /** 07 실종 신고 정보: 이동 경로(빨간 점선) + 마지막 위치 */
    LAST_SEEN,
}

/**
 * 지도 패널: 모의 좌표 위에 점·경로를 그린다 (코드A · 금진).
 *
 * 좌표는 모의데이터와 같은 m 단위 격자(원점 = 집). 보여줄 범위는 그릴 내용에 맞춰 자동으로 정한다.
 *
 * @param track 지금 외출의 이동 기록. 마지막 점이 현재(마지막 확인) 위치
 * @param usualRoutes 학습된 평소 동선 (외출 여러 개). 자주 다닌 길일수록 진하게 겹쳐 보인다
 * @param accent DEVIATION 모드에서 벗어난 경로 색 (보통 경보 단계 색)
 */
@Composable
fun MapPanel(
    mode: MapPanelMode,
    track: List<TrackPoint>,
    modifier: Modifier = Modifier,
    usualRoutes: List<List<TrackPoint>> = emptyList(),
    accent: Color = AdwdColors.Level2,
    landmarks: List<Landmark> = MockLandmarks.all,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = AdwdColors.Slate)

    Canvas(modifier.clipToBounds()) {
        if (size.width <= 0f || size.height <= 0f) return@Canvas
        val showUsual = mode != MapPanelMode.LAST_SEEN
        val showLandmarks = mode == MapPanelMode.HOME

        // 1) 보여줄 범위: 집 + 경로 (+ 평소 동선, 장소)
        val xs = ArrayList<Double>()
        val ys = ArrayList<Double>()
        fun include(x: Double, y: Double) { xs += x; ys += y }
        include(0.0, 0.0)
        track.forEach { include(it.x, it.y) }
        if (showUsual) usualRoutes.forEach { r -> r.forEach { include(it.x, it.y) } }
        if (showLandmarks) landmarks.forEach { include(it.x, it.y) }
        val view = MapViewport.fit(
            minX = xs.min(), minY = ys.min(), maxX = xs.max(), maxY = ys.max(),
            widthPx = size.width.toDouble(), heightPx = size.height.toDouble(),
            paddingPx = 22.dp.toPx().toDouble(),
        )

        // 2) 바탕: 격자 블록 + 공원
        drawRect(AdwdColors.MapBg)
        val gap = 4.dp.toPx()
        val step = view.blockStep(minBlockPx = 18.dp.toPx().toDouble())
        for (gx in view.blockRangeX(step)) for (gy in view.blockRangeY(step)) {
            worldRect(view, gx * step, gy * step, (gx + 1) * step, (gy + 1) * step, gap, AdwdColors.MapBlock)
        }
        val park = MockLandmarks.park
        worldRect(view, park.x, park.y, park.x + MockLandmarks.PARK_W, park.y + MockLandmarks.PARK_H, gap, AdwdColors.MapPark)

        // 3) 평소 동선 (반투명 청록을 겹쳐서, 자주 다닌 길이 진해짐)
        if (showUsual) {
            val usualStroke = Stroke(10.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            usualRoutes.forEach { r -> drawPath(pathOf(view, r), AdwdColors.Teal.copy(alpha = 0.22f), style = usualStroke) }
        }

        // 4) 지금 외출 경로
        if (track.size >= 2) {
            val (color, width, dashed) = when (mode) {
                MapPanelMode.HOME -> Triple(AdwdColors.Purple, 3.dp.toPx(), false)
                MapPanelMode.DEVIATION -> Triple(accent, 4.dp.toPx(), true)
                MapPanelMode.LAST_SEEN -> Triple(AdwdColors.Level3, 3.dp.toPx(), true)
            }
            val effect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())) else null
            drawPath(pathOf(view, track), color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = effect))
        }

        // 5) 장소 이름
        if (showLandmarks) {
            landmarks.forEach { lm ->
                val o = Offset(view.sx(lm.x), view.sy(lm.y))
                drawCircle(AdwdColors.Slate, 3.dp.toPx(), o)
                val layout = measurer.measure(lm.name, labelStyle)
                drawText(layout, topLeft = Offset(o.x + 5.dp.toPx(), o.y - layout.size.height - 1.dp.toPx()))
            }
        }

        // 6) 집
        homeMarker(Offset(view.sx(0.0), view.sy(0.0)))

        // 7) 현재(마지막 확인) 위치
        track.lastOrNull()?.let { p ->
            val o = Offset(view.sx(p.x), view.sy(p.y))
            val dot = when (mode) {
                MapPanelMode.HOME -> AdwdColors.Purple
                MapPanelMode.DEVIATION -> accent
                MapPanelMode.LAST_SEEN -> AdwdColors.Level3
            }
            drawCircle(Color.White, 9.dp.toPx(), o)
            drawCircle(dot, 6.dp.toPx(), o)
            if (mode == MapPanelMode.DEVIATION) drawCircle(AdwdColors.Purple, 6.dp.toPx(), o, style = Stroke(1.5.dp.toPx()))
        }
    }
}

private fun pathOf(view: MapViewport, points: List<TrackPoint>): Path = Path().apply {
    points.forEachIndexed { i, p ->
        if (i == 0) moveTo(view.sx(p.x), view.sy(p.y)) else lineTo(view.sx(p.x), view.sy(p.y))
    }
}

/** 좌표(m) 사각형을 화면 사각형으로 바꿔 [gap]만큼 안쪽으로 줄여 그린다 (블록 사이가 길처럼 보임) */
private fun DrawScope.worldRect(view: MapViewport, x0: Double, y0: Double, x1: Double, y1: Double, gap: Float, color: Color) {
    val left = min(view.sx(x0), view.sx(x1)) + gap / 2
    val right = max(view.sx(x0), view.sx(x1)) - gap / 2
    val top = min(view.sy(y0), view.sy(y1)) + gap / 2
    val bottom = max(view.sy(y0), view.sy(y1)) - gap / 2
    if (right <= left || bottom <= top) return
    drawRoundRect(color, Offset(left, top), Size(right - left, bottom - top), CornerRadius(3.dp.toPx()))
}

/** 보라 원 + 흰 집 모양 */
private fun DrawScope.homeMarker(c: Offset) {
    val u = 1.dp.toPx()
    drawCircle(AdwdColors.Purple, 12 * u, c)
    val house = Path().apply {
        moveTo(c.x - 6 * u, c.y + 1 * u); lineTo(c.x, c.y - 4 * u); lineTo(c.x + 6 * u, c.y + 1 * u)
        moveTo(c.x - 4 * u, c.y); lineTo(c.x - 4 * u, c.y + 6 * u); lineTo(c.x + 4 * u, c.y + 6 * u); lineTo(c.x + 4 * u, c.y)
    }
    drawPath(house, Color.White, style = Stroke(1.8f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
