package com.adwd.app.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.theme.AdwdColors

/**
 * 화면에서 쓰는 경보 단계. 색 + 모양(아이콘) + 숫자를 함께 쓴다.
 *
 * 코드B의 AlertState(4단계)와 1:1로 맞춘다. AlertState가 확정되면
 * [fromStep]으로 변환해서 쓰면 되고, 이 enum은 화면 표시용 정보만 갖는다.
 */
enum class AlertLevel(
    val step: Int,
    val label: String,
    val color: Color,
    val onColor: Color,
    /** 어르신 귀가 안내 화면 바탕색 */
    val elderBackground: Color,
    /** 어르신 귀가 안내 화면의 큰 원 색과 화살표 색 */
    val guideCircle: Color,
    val guideArrow: Color,
) {
    NORMAL(0, "정상", AdwdColors.Level0, AdwdColors.Purple, AdwdColors.Mint, AdwdColors.Teal, AdwdColors.Purple),
    CAUTION(1, "주의", AdwdColors.Level1, AdwdColors.Purple, AdwdColors.Mint, AdwdColors.Teal, AdwdColors.Purple),
    WARNING(2, "경고", AdwdColors.Level2, AdwdColors.Purple, AdwdColors.WarnBg, AdwdColors.Level2, AdwdColors.Purple),
    EMERGENCY(3, "긴급", AdwdColors.Level3, AdwdColors.White, AdwdColors.DangerBg, AdwdColors.Level3, AdwdColors.White);

    /** "2 경고" 처럼 숫자와 이름을 같이 쓴 표기 */
    val title: String get() = "$step $label"

    val icon: ImageVector
        get() = when (this) {
            NORMAL -> AdwdIcons.CheckCircle
            CAUTION -> AdwdIcons.Warning
            WARNING -> AdwdIcons.Bell
            EMERGENCY -> AdwdIcons.Danger
        }

    companion object {
        fun fromStep(step: Int): AlertLevel = entries.firstOrNull { it.step == step } ?: NORMAL
    }
}
