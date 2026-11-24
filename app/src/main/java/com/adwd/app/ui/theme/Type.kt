package com.adwd.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.adwd.app.R

/** Pretendard (SIL OFL 1.1). 라이선스 전문은 licenses/Pretendard-OFL.txt */
val Pretendard = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
    Font(R.font.pretendard_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, weight: FontWeight, lineHeight: Float? = null, letterSpacing: Float = 0f) = TextStyle(
    fontFamily = Pretendard,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight?.sp ?: TextStyle.Default.lineHeight,
    letterSpacing = letterSpacing.sp,
)

/** 피그마 텍스트 스타일과 같은 이름. */
object AdwdType {
    val ElderClock = style(56, FontWeight.ExtraBold, 61.6f, -1.1f)
    val ElderDisplay = style(32, FontWeight.ExtraBold, 41.6f, -0.3f)
    val ElderDirection = style(30, FontWeight.ExtraBold)
    val ElderButton = style(22, FontWeight.ExtraBold)
    val ElderBody = style(22, FontWeight.Bold, 30.8f)
    val ElderSub = style(20, FontWeight.SemiBold)

    val TitleLarge = style(26, FontWeight.ExtraBold, 34f)
    val TitleScreen = style(22, FontWeight.Bold)
    val TitleSection = style(18, FontWeight.Bold)
    val ButtonText = style(17, FontWeight.Bold)

    val BodyLarge = style(16, FontWeight.Normal)
    val BodyRegular = style(15, FontWeight.Normal, 24f)
    val BodyStrong = style(15, FontWeight.Bold)
    val BodySmall = style(13, FontWeight.Normal, 20.8f)
    val BodySmallStrong = style(13, FontWeight.Bold)
    val Caption = style(12, FontWeight.Normal)
    val Label = style(12, FontWeight.SemiBold)
    val LabelStrong = style(12, FontWeight.Bold)
    val Tab = style(11, FontWeight.SemiBold)
    val TabActive = style(11, FontWeight.ExtraBold)
}
