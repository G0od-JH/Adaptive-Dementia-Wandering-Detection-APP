package com.adwd.app.ui.screens.guardian

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.BackHeader
import com.adwd.app.ui.components.MapVariant
import com.adwd.app.ui.components.MockMap
import com.adwd.app.ui.components.PrimaryButton
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/**
 * 07 실종 신고 정보 (3단계 긴급).
 * 개인정보(사진, 인상착의)는 넣지 않고 마지막 위치와 최근 위치 기록만 보여준다.
 * 앱에서는 재생 중인 모의데이터의 마지막 위치·최근 기록을 넣는다 (AdwdNavHost).
 * TODO(코드A): MissingReportGenerator가 생기면 그 결과로 교체
 */
@Composable
fun MissingReportScreen(
    onBack: () -> Unit,
    /** 보호자가 상황을 확인하고 "괜찮아요"를 누름 → 엔진 resolve() (3단계는 수동 해제만) */
    onResolve: () -> Unit,
    lastSeenTime: String = "14:58",
    lastSeenPlace: String = "○○동 큰길 버스정류장 부근 (모의 좌표)",
    recent: List<Pair<String, String>> = FakeData.recentPlaces,
    /** 지도 자리 (코드A 지도 패널) */
    mapContent: @Composable () -> Unit = { MockMap(MapVariant.LAST_SEEN, Modifier.fillMaxSize()) },
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val level = AlertLevel.EMERGENCY

    Column(
        Modifier.fillMaxSize().background(AdwdColors.White).statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp),
    ) {
        BackHeader("실종 신고 정보", onBack)
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth().height(66.dp).clip(RoundedCornerShape(16.dp)).background(level.color).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(level.icon, contentDescription = null, tint = level.onColor, modifier = Modifier.size(34.dp))
            Spacer(Modifier.width(12.dp))
            Text("${level.title} · 연락 안 됨", style = AdwdType.TitleScreen.copy(fontSize = 21.sp), color = level.onColor)
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(AdwdIcons.Pin, contentDescription = null, tint = AdwdColors.Level3, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("마지막 확인 위치 · $lastSeenTime", style = AdwdType.BodySmallStrong.copy(fontSize = 14.sp), color = AdwdColors.Purple)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))) {
            mapContent()
        }
        Spacer(Modifier.height(8.dp))
        Text(lastSeenPlace, style = AdwdType.Caption, color = AdwdColors.Slate)
        Spacer(Modifier.height(14.dp))
        Text("최근 위치 기록", style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            recent.forEach { (t, place) ->
                Row {
                    Text(t, style = AdwdType.LabelStrong, color = AdwdColors.Slate, modifier = Modifier.width(52.dp))
                    Text(place, style = AdwdType.BodySmall, color = AdwdColors.Purple)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PrimaryButton(
                "112 신고", icon = AdwdIcons.Phone, container = AdwdColors.Level3, height = 54.dp,
                textStyle = AdwdType.ButtonText.copy(fontSize = 16.sp),
                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))) },
                modifier = Modifier.weight(1f),
            )
            PrimaryButton(
                "정보 복사", icon = AdwdIcons.Share, height = 54.dp,
                textStyle = AdwdType.ButtonText.copy(fontSize = 16.sp),
                onClick = {
                    val text = buildString {
                        appendLine("[ADWD 실종 신고 정보]")
                        appendLine("마지막 확인 위치 ($lastSeenTime): $lastSeenPlace")
                        recent.forEach { (t, p) -> appendLine("$t $p") }
                    }
                    clipboard.setText(AnnotatedString(text))
                    Toast.makeText(context, "신고 정보를 복사했어요", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            "괜찮아요, 어르신을 찾았어요", onClick = onResolve, container = AdwdColors.Mint, content = AdwdColors.Purple,
            textStyle = AdwdType.BodySmallStrong.copy(fontSize = 14.sp), height = 44.dp, modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
    }
}
