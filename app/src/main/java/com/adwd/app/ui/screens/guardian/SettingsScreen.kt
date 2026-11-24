package com.adwd.app.ui.screens.guardian

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adwd.app.ui.components.AdwdCheckbox
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.GuardianTab
import com.adwd.app.ui.components.LevelDot
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/**
 * 08 알림·연동 설정.
 * 거리·시간·심야 기준은 우리가 정하는 값이라 보호자 설정에 두지 않는다 (코드B 상수).
 */
@Composable
fun SettingsScreen(
    onTab: (GuardianTab) -> Unit,
    placeCount: Int = 0,
    onHomeLocation: () -> Unit = {},
    onPlaces: () -> Unit = {},
) {
    val targets = listOf(
        AlertLevel.CAUTION to "주의 · 어르신 화면에 귀가 안내",
        AlertLevel.WARNING to "경고 · 보호자 알림",
        AlertLevel.DANGER to "위험 · 실종 신고 정보 만들기",
    )
    // TODO(민규): 설정 저장
    val enabled = remember { mutableStateListOf(true, true, true) }

    GuardianScaffold(GuardianTab.SETTINGS, onTab) {
        Spacer(Modifier.height(8.dp))
        Text("설정", style = AdwdType.TitleScreen, color = AdwdColors.Purple)
        Spacer(Modifier.height(12.dp))
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AdwdColors.Card).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("단계별로 누구에게 알릴까요", style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
            targets.forEachIndexed { i, (level, text) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LevelDot(level)
                    Spacer(Modifier.width(8.dp))
                    Text(text, style = AdwdType.BodySmall, color = AdwdColors.Purple, modifier = Modifier.weight(1f))
                    AdwdCheckbox(enabled[i], { enabled[i] = it })
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        SettingRow(AdwdIcons.Link, "연결된 어르신", "${FakeData.elderId} · ${FakeData.linkCodeSpaced}")
        SettingRow(AdwdIcons.Home, "어르신 집 위치", "›", onClick = onHomeLocation)
        SettingRow(AdwdIcons.Pin, "자주 가는 장소", "${placeCount}곳 ›", onClick = onPlaces)
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().height(40.dp).let { if (onClick != null) it.clickable(onClick = onClick) else it },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = AdwdColors.Slate, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(title, style = AdwdType.BodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = AdwdType.BodyLarge.fontSize * (14f / 16f)), color = AdwdColors.Purple, modifier = Modifier.weight(1f))
        Text(value, style = AdwdType.BodySmallStrong, color = if (value.endsWith("›")) AdwdColors.Slate else AdwdColors.Purple)
    }
}
