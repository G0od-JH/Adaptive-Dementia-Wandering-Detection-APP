package com.adwd.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * 피그마 Component 페이지의 Icon/ 컴포넌트와 같은 아이콘 세트.
 * 모두 24 단위, 선 2px, 끝 둥글게. 색은 Icon(tint = ...)로 바꾼다.
 */
object AdwdIcons {
    private fun circle(cx: Float, cy: Float, r: Float) =
        "M${cx - r},$cy a$r,$r 0 1,0 ${2 * r},0 a$r,$r 0 1,0 ${-2 * r},0"

    private fun line(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach {
                addPath(
                    pathData = addPathNodes(it),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    val Home = line("home", "M3 11l9-7 9 7", "M5 10v10h14V10")
    val Map = line("map", "M9 4L3 6v14l6-2 6 2 6-2V4l-6 2-6-2z", "M9 4v14M15 6v14")
    val Pin = line("pin", "M 12 21 s -7 -6.5 -7 -12 a 7 7 0 0 1 14 0 c 0 5.5 -7 12 -7 12 z", circle(12f, 9f, 2.5f))
    val Bell = line("bell", "M 6 16 v -5 a 6 6 0 0 1 12 0 v 5 l 2 2 H 4 l 2 -2 z", "M10 21h4")
    val Link = line("link", "M 10 14 a 4 4 0 0 0 5.7 0 l 3 -3 a 4 4 0 0 0 -5.7 -5.7 l -1 1", "M 14 10 a 4 4 0 0 0 -5.7 0 l -3 3 a 4 4 0 0 0 5.7 5.7 l 1 -1")
    val Settings = line("settings", "M4 6h10M18 6h2M4 12h4M12 12h8M4 18h12", circle(16f, 6f, 2f), circle(10f, 12f, 2f), circle(18f, 18f, 2f))
    val Phone = line("phone", "M 5 4 h 4 l 2 5 -2.5 1.5 a 11 11 0 0 0 5 5 L 15 13 l 5 2 v 4 a 2 2 0 0 1 -2 2 A 16 16 0 0 1 3 6 a 2 2 0 0 1 2 -2 z")
    val Logout = line("logout", "M 9 21 H 5 a 2 2 0 0 1 -2 -2 V 5 a 2 2 0 0 1 2 -2 h 4", "M16 17l5-5-5-5", "M21 12H9")
    val Lock = line("lock", "M 7 11 h 10 a 2 2 0 0 1 2 2 v 6 a 2 2 0 0 1 -2 2 H 7 a 2 2 0 0 1 -2 -2 v -6 a 2 2 0 0 1 2 -2 z", "M 8 11 V 7 a 4 4 0 0 1 8 0 v 4")
    val Navigate = line("navigate", "M12 3l7 18-7-4-7 4z")
    val Clock = line("clock", circle(12f, 12f, 9f), "M12 7v5l3 2")
    val Moon = line("moon", "M 20 14 A 8 8 0 0 1 10 4 a 8 8 0 1 0 10 10 z")
    val Repeat = line("repeat", "M17 2l4 4-4 4", "M 3 11 V 9 a 3 3 0 0 1 3 -3 h 15 M 7 22 l -4 -4 4 -4", "M 21 13 v 2 a 3 3 0 0 1 -3 3 H 3")
    val Route = line("route", circle(6f, 19f, 2f), circle(18f, 5f, 2f), "M 8 19 h 7 a 3.5 3.5 0 0 0 0 -7 H 9 a 3.5 3.5 0 0 1 0 -7 h 7")
    val Person = line("person", circle(12f, 8f, 4f), "M 4 21 a 8 8 0 0 1 16 0")
    val Share = line("share", circle(18f, 5f, 2.5f), circle(6f, 12f, 2.5f), circle(18f, 19f, 2.5f), "M8.2 10.8l7.6-4.4M8.2 13.2l7.6 4.4")
    val Speed = line("speed", "M3 5l8 7-8 7zM13 5l8 7-8 7z")
    val Pause = line("pause", "M8 4v16M16 4v16")

    val CheckCircle = line("check-circle", circle(12f, 12f, 9f), "M8 12l3 3 5-6")
    val Warning = line("warning", "M12 3L2 20h20L12 3z", "M12 10v4M12 17v.01")
    val Danger = line("danger", "M8 3h8l5 5v8l-5 5H8l-5-5V8z", "M12 8v5M12 16v.01")
    val Back = line("back", "M15 5l-7 7 7 7")
    val ChevronRight = line("chevron-right", "M9 5l7 7-7 7")
    val Plus = line("plus", "M12 5v14M5 12h14")
    val Trash = line("trash", "M4 7h16", "M10 11v6M14 11v6", "M5 7l1 13h12l1-13", "M9 7V4h6v3")
    val Hospital = line("hospital", "M4 21V5h16v16", "M12 8v6M9 11h6", "M2 21h20")
    val Market = line("market", "M3 9l2-5h14l2 5", "M4 9v11h16V9", "M3 9h18", "M10 20v-5h4v5")
    val Tree = line("tree", "M12 3l6 9H6z", "M12 9l7 9H5z", "M12 18v3")

    /** 어르신 귀가 안내의 큰 방향 화살표. 면으로 채워져 있고, rotate로 방향을 바꾼다(0 = 위쪽). */
    val DirectionArrow: ImageVector =
        ImageVector.Builder("direction", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(
                pathData = addPathNodes("M12 3l7 18-7-4-7 4z"),
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
}
