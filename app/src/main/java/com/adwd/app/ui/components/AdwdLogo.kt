package com.adwd.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import com.adwd.app.ui.theme.AdwdColors

private val housePath = PathParser().parsePathString("M19 31 L32 19.5 L45 31 V46 H19 Z").toPath()
private val stepsPath = PathParser().parsePathString("M7 57 C14 52 20 55 25 50 S30 45 32 43").toPath()

/**
 * ADWD 로고. 집 모양(돌아갈 곳) 안으로 점선 발자국이 돌아오는 형태.
 * 스플래시는 background = Slate, 나머지는 Purple.
 */
@Composable
fun AdwdLogo(size: Dp, modifier: Modifier = Modifier, background: Color = AdwdColors.Purple) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension / 64f
        scale(s, pivot = Offset.Zero) {
            drawRoundRect(background, cornerRadius = CornerRadius(18f, 18f), size = androidx.compose.ui.geometry.Size(64f, 64f))
            drawPath(housePath, AdwdColors.Teal, style = Stroke(width = 4f, join = StrokeJoin.Round))
            drawPath(
                stepsPath, AdwdColors.Mint,
                style = Stroke(width = 3f, cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(0.1f, 6f))),
            )
            drawCircle(AdwdColors.Mint, radius = 4f, center = Offset(32f, 40f))
        }
    }
}
