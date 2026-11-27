package com.adwd.app.ui.screens.guardian

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.GuardianTab
import com.adwd.app.ui.components.GuardianTabBar
import com.adwd.app.ui.components.MapVariant
import com.adwd.app.ui.components.MockMap
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/** 보호자 화면 공통 틀: 내용 + 아래 탭바 */
@Composable
internal fun GuardianScaffold(tab: GuardianTab, onTab: (GuardianTab) -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().background(AdwdColors.White)) {
        Column(Modifier.weight(1f).statusBarsPadding().padding(horizontal = 18.dp), content = content)
        GuardianTabBar(selected = tab, onSelect = onTab)
    }
}

/**
 * 05 보호자 홈(지도).
 * @param mapContent 지도 자리. 앱에서는 모의데이터 좌표로 그린 [com.adwd.app.ui.components.TrajectoryMap]을 넣는다.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GuardianHomeScreen(
    level: AlertLevel,
    reason: String,
    onOpenAlert: (AlertLevel) -> Unit,
    onBell: () -> Unit,
    onTab: (GuardianTab) -> Unit,
    onOpenDemo: () -> Unit,
    mapContent: @Composable () -> Unit = { MockMap(MapVariant.HOME, Modifier.fillMaxSize()) },
) {
    GuardianScaffold(GuardianTab.HOME, onTab) {
        Spacer(Modifier.height(4.dp))
        // 머리줄: 연결된 어르신 + 알림 종. 아바타를 길게 누르면 11 데모 패널 (발표 시연용)
        Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(AdwdColors.Mint)
                    .combinedClickable(onClick = {}, onLongClick = onOpenDemo),
                contentAlignment = Alignment.Center,
            ) { Icon(AdwdIcons.Person, contentDescription = "어르신", tint = AdwdColors.Purple, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(12.dp))
            Text("${FakeData.elderId} 님", style = AdwdType.TitleScreen.copy(fontSize = AdwdType.TitleScreen.fontSize * (20f / 22f)), color = AdwdColors.Purple, modifier = Modifier.weight(1f))
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onBell), contentAlignment = Alignment.Center) {
                Icon(AdwdIcons.Bell, contentDescription = "알림", tint = AdwdColors.Purple, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        StatusCard(level, reason, onClick = { onOpenAlert(level) })
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(16.dp))) {
            mapContent()
            Text(
                "지도 패널", style = AdwdType.Tab, color = AdwdColors.White,
                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).clip(RoundedCornerShape(10.dp)).background(AdwdColors.Purple).padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Row(Modifier.align(Alignment.BottomStart).padding(10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Legend("● 현재 위치", AdwdColors.Purple)
                Legend("━ 오늘 이동 경로", AdwdColors.Slate)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun statusText(level: AlertLevel, reason: String) = when (level) {
    AlertLevel.NORMAL -> "평소 산책 경로 안에 있어요"
    AlertLevel.CAUTION -> "$reason · 어르신께 귀가 안내 중"
    AlertLevel.WARNING -> "$reason · 눌러서 자세히 보기"
    AlertLevel.EMERGENCY -> "연락이 안 돼요 · 실종 신고 정보 보기"
}

@Composable
private fun StatusCard(level: AlertLevel, reason: String, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(level.color).clickable(onClick = onClick).padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(level.icon, contentDescription = null, tint = level.onColor, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text(level.title, style = AdwdType.TitleScreen.copy(fontSize = AdwdType.TitleScreen.fontSize * (20f / 22f)), color = level.onColor)
                Text(statusText(level, reason), style = AdwdType.BodySmall, color = level.onColor)
            }
        }
        Spacer(Modifier.height(12.dp))
        // 4단계 진행 막대
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            AlertLevel.entries.forEach { l ->
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                            .background(if (l.step <= level.step) level.onColor else level.onColor.copy(alpha = 0.18f)),
                    )
                    Text(l.label, style = AdwdType.Tab, color = level.onColor)
                }
            }
        }
    }
}

@Composable
private fun Legend(text: String, color: Color) {
    Text(
        text, style = AdwdType.Tab, color = color,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(AdwdColors.White).padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
