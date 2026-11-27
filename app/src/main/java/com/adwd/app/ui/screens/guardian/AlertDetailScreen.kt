package com.adwd.app.ui.screens.guardian

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.BackHeader
import com.adwd.app.ui.components.MapVariant
import com.adwd.app.ui.components.MockMap
import com.adwd.app.ui.components.PrimaryButton
import com.adwd.app.ui.components.SecondaryButton
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/**
 * 06 경보 상세 (2단계 경고) / 06-1 경보 상세 (1단계 주의).
 * 기록마다 상세가 다르다: 왜 울렸는지(판단 이유), 벗어난 경로, 시간순 기록, 대응 버튼.
 * - 1·2단계: 전화하기만. 평소 동선으로 돌아오면 엔진이 자동으로 해제한다
 * - 3단계 긴급: 전화하기 + 실종 신고 정보 + "괜찮아요"(보호자 수동 해제)
 * - 이미 끝난 기록(resolved)은 어떻게 끝났는지 보여준다
 * TODO(코드B): 판단 엔진이 남긴 기록으로 FakeData.alertDetails 교체
 */
@Composable
fun AlertDetailScreen(
    detail: FakeData.AlertDetail,
    onBack: () -> Unit,
    onMissingReport: () -> Unit,
    onDismiss: () -> Unit,
    /** 지도 자리 (코드A 지도 패널). 경보 단계 색을 받아 벗어난 경로를 그 색으로 그린다 */
    mapContent: @Composable (accent: androidx.compose.ui.graphics.Color) -> Unit = { accent ->
        MockMap(MapVariant.DEVIATION, Modifier.fillMaxSize(), accent = accent)
    },
) {
    val context = LocalContext.current
    val level = detail.level
    Column(
        Modifier.fillMaxSize().background(AdwdColors.White).statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp),
    ) {
        BackHeader("경보 상세", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().height(66.dp).clip(RoundedCornerShape(16.dp)).background(level.color).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(level.icon, contentDescription = null, tint = level.onColor, modifier = Modifier.size(34.dp))
                Spacer(Modifier.width(12.dp))
                Text("${level.title} · ${detail.reason}", style = AdwdType.TitleScreen.copy(fontSize = 21.sp), color = level.onColor)
            }
            if (level == AlertLevel.CAUTION) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "1단계 주의는 어르신 화면에 귀가 안내만 보낸 단계예요. 보호자 알림은 가지 않았어요.",
                    style = AdwdType.BodySmall, color = AdwdColors.Slate,
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("왜 경보가 울렸나요?", style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                detail.reasons.forEach { r ->
                    Row(
                        Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(12.dp)).background(AdwdColors.Card).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(r.kind.icon(), contentDescription = null, tint = AdwdColors.Purple, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(r.label, style = AdwdType.BodySmall, color = AdwdColors.Purple, modifier = Modifier.weight(1f))
                        Text(
                            r.value, style = AdwdType.LabelStrong, color = AdwdColors.Purple,
                            modifier = Modifier.clip(RoundedCornerShape(9.dp)).background(if (r.exceeded) level.color else AdwdColors.Mint).padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(14.dp))) {
                mapContent(level.color)
            }

            Spacer(Modifier.height(14.dp))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(AdwdColors.Card).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                detail.timeline.forEachIndexed { i, t ->
                    val last = i == detail.timeline.lastIndex
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(t.time, style = AdwdType.LabelStrong, color = AdwdColors.Slate, modifier = Modifier.width(46.dp))
                        Box(Modifier.size(12.dp).clip(CircleShape).background(t.level.color))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${t.level.title} · ${t.text}",
                            style = if (last) AdwdType.BodySmall.copy(fontWeight = FontWeight.Bold) else AdwdType.BodySmall,
                            color = AdwdColors.Purple,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(
                "전화하기", icon = AdwdIcons.Phone, textStyle = AdwdType.BodyStrong, height = 52.dp,
                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))) },
                modifier = Modifier.weight(1f),
            )
            if (level == AlertLevel.EMERGENCY) {
                SecondaryButton("실종 신고 정보", onClick = onMissingReport, content = AdwdColors.Level3, textStyle = AdwdType.BodyStrong, height = 52.dp, modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
        val resolved = detail.resolved
        if (resolved == null && level == AlertLevel.EMERGENCY) {
            PrimaryButton(
                "괜찮아요, 알고 있는 외출이에요", onClick = onDismiss, container = AdwdColors.Mint, content = AdwdColors.Purple,
                textStyle = AdwdType.BodySmallStrong.copy(fontSize = 14.sp), height = 44.dp, modifier = Modifier.fillMaxWidth(),
            )
        } else if (resolved == null) {
            Text(
                "평소 동선으로 돌아오면 경보가 자동으로 풀려요.",
                style = AdwdType.Caption, color = AdwdColors.Slate,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )
        } else {
            Row(
                Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp)).background(AdwdColors.Canvas).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(AdwdIcons.CheckCircle, contentDescription = null, tint = AdwdColors.Slate, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("끝난 기록 · $resolved", style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun FakeData.ReasonKind.icon() = when (this) {
    FakeData.ReasonKind.DISTANCE -> AdwdIcons.Route
    FakeData.ReasonKind.DURATION -> AdwdIcons.Clock
    FakeData.ReasonKind.TIME_OF_DAY -> AdwdIcons.Moon
    FakeData.ReasonKind.REPEAT -> AdwdIcons.Repeat
}
