package com.adwd.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.Pretendard
import kotlin.math.max

/**
 * 지도 자리에 들어가는 모의 지도 그림.
 *
 * 실제 지도 패널(점·경로 그리기)은 코드A(금진)가 만든다. 그게 오면 각 화면의 `mapContent` 자리에
 * 그 컴포넌트를 넣고 이 파일은 지워도 된다. 좌표는 350 단위 격자 기준이고, 화면 크기에 맞춰
 * 가운데를 기준으로 잘라서(crop) 그린다.
 */
enum class MapVariant(val viewW: Float, val viewH: Float) {
    /** 05 보호자 홈: 학습된 동선 + 현재 위치 */
    HOME(350f, 260f),
    /** 06 경보 상세: 평소 동선 + 벗어난 경로(주황 점선) */
    DEVIATION(350f, 150f),
    /** 07 실종 신고 정보: 이동 경로(빨간 점선) + 마지막 위치 */
    LAST_SEEN(350f, 112f),
}

private fun p(d: String) = PathParser().parsePathString(d).toPath()

@Composable
fun MockMap(
    variant: MapVariant,
    modifier: Modifier = Modifier,
    accent: Color = AdwdColors.Level2,
    /** false면 HOME 지도의 동선·집·현재 위치를 빼고 바탕만 그린다 (04-2 장소 찍기용) */
    showMarkers: Boolean = true,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontFamily = Pretendard, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, color = AdwdColors.Slate)
    Canvas(modifier.clipToBounds()) {
        val s = max(size.width / variant.viewW, size.height / variant.viewH)
        val dx = (size.width - variant.viewW * s) / 2f
        val dy = (size.height - variant.viewH * s) / 2f
        translate(dx, dy) {
            scale(s, pivot = Offset.Zero) {
                drawRect(AdwdColors.MapBg, size = Size(variant.viewW, variant.viewH))
                when (variant) {
                    MapVariant.HOME -> {
                        blocks(listOf(
                            floatArrayOf(8f, 8f, 84f, 64f), floatArrayOf(108f, 8f, 104f, 64f), floatArrayOf(228f, 8f, 114f, 64f),
                            floatArrayOf(8f, 88f, 84f, 64f), floatArrayOf(108f, 88f, 104f, 64f),
                            floatArrayOf(8f, 168f, 84f, 84f), floatArrayOf(108f, 168f, 104f, 84f), floatArrayOf(228f, 168f, 114f, 84f),
                        ))
                        drawRoundRect(AdwdColors.MapPark, Offset(228f, 88f), Size(114f, 64f), CornerRadius(6f))
                        if (showMarkers) {
                            // 학습된 동선
                            drawPath(p("M50 205 V160 H100 V80 H220 V40 H262"), AdwdColors.Teal.copy(alpha = 0.7f),
                                style = Stroke(12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            // 집
                            drawCircle(AdwdColors.Purple, 13f, Offset(50f, 205f))
                            drawPath(p("M44 206 l6-5 6 5 M46 205 v6 h8 v-6"), Color.White, style = Stroke(1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                            // 현재 위치
                            drawCircle(Color.White, 12f, Offset(160f, 80f))
                            drawCircle(AdwdColors.Purple, 8f, Offset(160f, 80f))
                        }
                    }
                    MapVariant.DEVIATION -> {
                        blocks(listOf(
                            floatArrayOf(8f, 8f, 84f, 54f), floatArrayOf(108f, 8f, 104f, 54f), floatArrayOf(228f, 8f, 114f, 54f),
                            floatArrayOf(8f, 78f, 84f, 64f), floatArrayOf(108f, 78f, 104f, 64f), floatArrayOf(228f, 78f, 114f, 64f),
                        ))
                        drawPath(p("M50 120 V70 H220 V30 H262"), AdwdColors.Teal.copy(alpha = 0.7f),
                            style = Stroke(10f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                        drawPath(p("M220 70 H300 V130 H340"), accent,
                            style = Stroke(4f, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
                        drawCircle(AdwdColors.Purple, 10f, Offset(50f, 120f))
                        drawCircle(Color.White, 11f, Offset(330f, 130f))
                        drawCircle(accent, 7f, Offset(330f, 130f))
                        drawCircle(AdwdColors.Purple, 7f, Offset(330f, 130f), style = Stroke(1.5f))
                    }
                    MapVariant.LAST_SEEN -> {
                        blocks(listOf(
                            floatArrayOf(8f, 8f, 84f, 40f), floatArrayOf(108f, 8f, 104f, 40f), floatArrayOf(228f, 8f, 114f, 40f),
                            floatArrayOf(8f, 64f, 84f, 40f), floatArrayOf(108f, 64f, 104f, 40f), floatArrayOf(228f, 64f, 114f, 40f),
                        ))
                        drawPath(p("M40 56 H160 V30 H220 V56 H330"), AdwdColors.Level3,
                            style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))))
                        drawCircle(Color.White, 12f, Offset(330f, 56f))
                        drawCircle(AdwdColors.Level3, 8f, Offset(330f, 56f))
                    }
                }
            }
            if (variant == MapVariant.HOME && showMarkers) {
                // 장소 이름은 확대하지 않고 실제 크기로 그린다
                listOf("공원" to Offset(270f, 124f), "시장" to Offset(262f, 44f), "경로당" to Offset(138f, 124f)).forEach { (t, o) ->
                    val layout = measurer.measure(t, labelStyle)
                    drawText(layout, topLeft = Offset(o.x * s, o.y * s - layout.size.height * 0.8f))
                }
            }
        }
    }
}

private fun DrawScope.blocks(rects: List<FloatArray>) {
    rects.forEach { r -> drawRoundRect(AdwdColors.MapBlock, Offset(r[0], r[1]), Size(r[2], r[3]), CornerRadius(6f)) }
}
