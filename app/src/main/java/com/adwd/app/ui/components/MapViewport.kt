package com.adwd.app.ui.components

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * 모의 좌표(m, 원점 = 집, y = 북쪽) ↔ 화면 픽셀 변환 (코드A · 금진).
 * 화면은 y가 아래로 커지므로 북쪽이 위로 오게 뒤집는다. 가로세로 비율은 유지한다.
 */
class MapViewport(
    private val minX: Double,
    private val maxY: Double,
    /** 1m = 몇 px */
    val scale: Double,
    private val offsetX: Double,
    private val offsetY: Double,
    val widthPx: Double,
    val heightPx: Double,
) {
    fun sx(x: Double): Float = (offsetX + (x - minX) * scale).toFloat()
    fun sy(y: Double): Float = (offsetY + (maxY - y) * scale).toFloat()

    /** 화면에 실제로 보이는 좌표 범위 (m) */
    val visibleMinX: Double get() = minX - offsetX / scale
    val visibleMaxX: Double get() = minX + (widthPx - offsetX) / scale
    val visibleMaxY: Double get() = maxY + offsetY / scale
    val visibleMinY: Double get() = maxY - (heightPx - offsetY) / scale

    /** 블록 한 칸 크기(m). 화면에서 [minBlockPx] 이상이 되는 가장 작은 값 */
    fun blockStep(minBlockPx: Double): Double =
        BLOCK_STEPS.firstOrNull { it * scale >= minBlockPx } ?: BLOCK_STEPS.last()

    /** 보이는 범위를 덮는 블록 칸 번호 범위 */
    fun blockRangeX(step: Double): IntRange = floor(visibleMinX / step).toInt()..ceil(visibleMaxX / step).toInt()
    fun blockRangeY(step: Double): IntRange = floor(visibleMinY / step).toInt()..ceil(visibleMaxY / step).toInt()

    companion object {
        /** 격자 도로 간격 후보 (m). 모의데이터 길이 50m 격자 위에 있다 */
        val BLOCK_STEPS = listOf(50.0, 100.0, 200.0, 400.0, 800.0)

        /**
         * 주어진 범위(m)가 [paddingPx] 여백을 두고 화면 안에 다 들어오게 맞춘다.
         * 범위가 너무 좁으면(점 하나 등) [minSpanM]까지 넓혀서 과하게 확대되지 않게 한다.
         */
        fun fit(
            minX: Double, minY: Double, maxX: Double, maxY: Double,
            widthPx: Double, heightPx: Double, paddingPx: Double,
            minSpanM: Double = 300.0,
        ): MapViewport {
            require(widthPx > 0 && heightPx > 0) { "지도 크기가 0입니다: ${widthPx}x$heightPx" }
            require(minX <= maxX && minY <= maxY) { "지도 범위가 뒤집혔습니다" }
            val cx = (minX + maxX) / 2
            val cy = (minY + maxY) / 2
            val spanX = max(maxX - minX, minSpanM)
            val spanY = max(maxY - minY, minSpanM)
            val usableW = max(widthPx - 2 * paddingPx, 1.0)
            val usableH = max(heightPx - 2 * paddingPx, 1.0)
            val scale = min(usableW / spanX, usableH / spanY)
            val left = cx - spanX / 2
            val top = cy + spanY / 2
            return MapViewport(
                minX = left,
                maxY = top,
                scale = scale,
                offsetX = (widthPx - spanX * scale) / 2,
                offsetY = (heightPx - spanY * scale) / 2,
                widthPx = widthPx,
                heightPx = heightPx,
            )
        }
    }
}
