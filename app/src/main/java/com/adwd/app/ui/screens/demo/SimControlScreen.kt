package com.adwd.app.ui.screens.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.state.AdwdUiState
import com.adwd.app.ui.state.SimScenario
import com.adwd.app.ui.state.SimSpeed
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/**
 * 11 시뮬레이션 데모 패널 (발표 시연용).
 * 시나리오·배속을 고르고, 경보 단계를 직접 바꿔서 어르신/보호자 화면 전환을 보여준다.
 * TODO(코드A): 시뮬레이션 시계·재생과 연결. TODO(코드B): 판단 로그를 엔진에서 받기
 */
@Composable
fun SimControlScreen(
    state: AdwdUiState,
    onScenario: (SimScenario) -> Unit,
    onSpeed: (SimSpeed) -> Unit,
    onTogglePlay: () -> Unit,
    onLevel: (AlertLevel) -> Unit,
    onBack: () -> Unit,
    log: List<Pair<String, Int>> = FakeData.simLog,
) {
    val mint = AdwdColors.Mint
    val section = AdwdType.LabelStrong.copy(letterSpacing = 0.7.sp)
    Column(
        Modifier.fillMaxSize().background(AdwdColors.Purple).statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(52.dp).clickable(onClick = onBack), verticalAlignment = Alignment.CenterVertically) {
            Icon(AdwdIcons.Speed, contentDescription = null, tint = AdwdColors.Teal, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("시뮬레이션 데모 패널", style = AdwdType.TitleSection, color = AdwdColors.White)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(12.dp))
            Text("시나리오", style = section, color = AdwdColors.Teal)
            Spacer(Modifier.height(8.dp))
            SimScenario.entries.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 6.dp)) {
                    row.forEach { s ->
                        val on = s == state.scenario
                        Box(
                            Modifier.weight(1f).height(37.dp).clip(RoundedCornerShape(10.dp))
                                .background(if (on) AdwdColors.Teal else Color.Transparent)
                                .border(1.dp, if (on) AdwdColors.Teal else mint.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .clickable { onScenario(s) }.padding(horizontal = 13.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) { Text(s.label, style = AdwdType.BodySmall.copy(fontWeight = FontWeight.SemiBold), color = if (on) AdwdColors.Purple else mint) }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(72.dp).clip(RoundedCornerShape(12.dp)).background(mint.copy(alpha = 0.08f)).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${state.simDay}일차", style = AdwdType.BodySmallStrong, color = AdwdColors.Teal)
                Spacer(Modifier.width(10.dp))
                Text(state.simTime, style = AdwdType.ElderClock.copy(fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.8).sp), color = AdwdColors.White, modifier = Modifier.weight(1f))
                Text("학습 30일 + 시연 1일", style = AdwdType.Caption, color = mint)
            }

            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ControlButton(if (state.isPlaying) "일시정지" else "재생", selected = true, icon = if (state.isPlaying) AdwdIcons.Pause else AdwdIcons.Speed, modifier = Modifier.weight(1.4f), onClick = onTogglePlay)
                SimSpeed.entries.forEach { speed ->
                    ControlButton(speed.label, selected = false, highlighted = speed == state.speed, modifier = Modifier.weight(1f), onClick = { onSpeed(speed) })
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("현재 경보 단계", style = section, color = AdwdColors.Teal)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AlertLevel.entries.forEach { l ->
                    val on = l == state.level
                    Box(
                        Modifier.weight(1f).height(30.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (on) l.color else l.color.copy(alpha = 0.25f)).clickable { onLevel(l) },
                        contentAlignment = Alignment.Center,
                    ) { Text(l.title, style = AdwdType.LabelStrong, color = if (on) l.onColor else mint) }
                }
            }

            Spacer(Modifier.height(18.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.25f)).padding(14.dp),
            ) {
                Text("판단 로그", style = section, color = AdwdColors.Teal)
                Spacer(Modifier.height(8.dp))
                log.forEach { (line, lv) ->
                    val warn = lv >= 2
                    Text(
                        line,
                        style = AdwdType.Tab.copy(fontWeight = if (warn) FontWeight.Bold else FontWeight.Normal, lineHeight = 18.7.sp),
                        color = if (warn) AdwdColors.Level2 else mint,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ControlButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    val bg = when {
        selected -> AdwdColors.Teal
        highlighted -> AdwdColors.Mint
        else -> AdwdColors.Mint.copy(alpha = 0.12f)
    }
    val fg = if (selected || highlighted) AdwdColors.Purple else AdwdColors.Mint
    Row(
        modifier.height(44.dp).clip(RoundedCornerShape(10.dp)).background(bg).clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = AdwdType.BodySmallStrong.copy(fontSize = 14.sp, fontWeight = FontWeight.ExtraBold), color = fg)
    }
}
