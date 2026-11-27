package com.adwd.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.engine.TrackPoint
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.Pretendard
import kotlin.math.max
import kotlin.math.min

/** 집 반경. 이 안의 점은 "집에 있음"으로 보고 경로를 끊는다 (엔진 thresholds::kHomeRadiusM 와 같은 값) */
private const val HOME_RADIUS_M = 50.0
/** 격자 한 칸 (엔진 thresholds::kGridCellM 와 같은 값) */
private const val GRID_M = 50.0
/** 너무 확대되지 않게 최소로 보여줄 폭(m) */
private const val MIN_SPAN_M = 400.0

/**
 * 모의데이터 좌표로 그리는 지도 패널.
 * - 좌표: 미터, 원점(0,0) = 집, 위쪽 = 북쪽 (금진 데이터 포맷)
 * - 집을 떠난 구간만 경로로 그리고, 집 반경(50m)에 들어오면 경로를 끊는다
 * - 화면 크기에 맞춰 자동으로 확대/축소, 오른쪽 아래에 축척 막대
 */
@Composable
fun TrajectoryMap(
    track: List<TrackPoint>,
    current: TrackPoint?,
    modifier: Modifier = Modifier,
    /** 현재 위치 점 색 (경보 단계 색) */
    accent: Color = AdwdColors.Purple,
    pathColor: Color = AdwdColors.Teal,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = AdwdColors.Slate)

    Canvas(modifier.clipToBounds()) {
        drawRect(AdwdColors.MapBg)

        // 보여줄 범위: 집 + 모든 점 (+ 여유 15%)
        var minX = 0.0; var maxX = 0.0; var minY = 0.0; var maxY = 0.0
        track.forEach { minX = min(minX, it.x); maxX = max(maxX, it.x); minY = min(minY, it.y); maxY = max(maxY, it.y) }
        val spanX = max(maxX - minX, MIN_SPAN_M) * 1.3
        val spanY = max(maxY - minY, MIN_SPAN_M) * 1.3
        val scale = min(size.width / spanX, size.height / spanY).toFloat()  // px per m
        val cx = ((minX + maxX) / 2).toFloat()
        val cy = ((minY + maxY) / 2).toFloat()
        fun toScreen(x: Double, y: Double) = Offset(
            size.width / 2f + (x.toFloat() - cx) * scale,
            size.height / 2f - (y.toFloat() - cy) * scale,  // 북쪽이 위
        )

        // 격자 (칸이 너무 작아지면 2칸, 4칸씩 묶어서)
        var gridPx = (GRID_M * scale).toFloat()
        while (gridPx < 14f) gridPx *= 2
        val origin = toScreen(0.0, 0.0)
        val gridColor = AdwdColors.MapBlock
        var gx = origin.x % gridPx
        while (gx < size.width) { drawLine(gridColor, Offset(gx, 0f), Offset(gx, size.height), 1f); gx += gridPx }
        var gy = origin.y % gridPx
        while (gy < size.height) { drawLine(gridColor, Offset(0f, gy), Offset(size.width, gy), 1f); gy += gridPx }

        // 경로: 집을 나선 구간마다 따로 그림
        val stroke = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        var path: Path? = null
        track.forEach { p ->
            if (p.distanceFromHome <= HOME_RADIUS_M) {
                path?.let { it.lineTo(origin.x, origin.y); drawPath(it, pathColor.copy(alpha = 0.75f), style = stroke) }
                path = null
            } else {
                val o = toScreen(p.x, p.y)
                val seg = path ?: Path().also { it.moveTo(origin.x, origin.y); path = it }
                seg.lineTo(o.x, o.y)
            }
        }
        path?.let { drawPath(it, pathColor.copy(alpha = 0.75f), style = stroke) }

        // 집
        drawCircle(AdwdColors.Purple, 9.dp.toPx(), origin)
        val h = 3.5.dp.toPx()
        val house = Path().apply {
            moveTo(origin.x - h, origin.y); lineTo(origin.x, origin.y - h); lineTo(origin.x + h, origin.y)
            moveTo(origin.x - h * 0.7f, origin.y); lineTo(origin.x - h * 0.7f, origin.y + h); lineTo(origin.x + h * 0.7f, origin.y + h); lineTo(origin.x + h * 0.7f, origin.y)
        }
        drawPath(house, Color.White, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // 현재 위치 (집 안이면 집 위에 겹쳐 그림)
        current?.let {
            val o = if (it.distanceFromHome <= HOME_RADIUS_M) origin else toScreen(it.x, it.y)
            drawCircle(Color.White, 9.dp.toPx(), o)
            drawCircle(accent, 6.dp.toPx(), o)
        }

        // 축척 막대 (지도 폭의 약 1/4)
        val target = size.width / 4f / scale
        val barM = listOf(50, 100, 200, 300, 500, 1000, 2000).lastOrNull { it <= target } ?: 50
        val barPx = barM * scale
        val margin = 10.dp.toPx()
        val y = size.height - margin
        val x1 = size.width - margin
        drawLine(AdwdColors.Slate, Offset(x1 - barPx, y), Offset(x1, y), 2.dp.toPx(), cap = StrokeCap.Round)
        val label = if (barM >= 1000) "${barM / 1000}km" else "${barM}m"
        val layout = measurer.measure(label, labelStyle)
        drawText(layout, topLeft = Offset(x1 - barPx / 2f - layout.size.width / 2f, y - layout.size.height - 2.dp.toPx()))
    }
}
