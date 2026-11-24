package com.adwd.app.ui.screens.elder

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.ElderCallButton
import com.adwd.app.ui.components.StatusChip
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 09 어르신 홈(경보 없음). 할 일이 없다는 걸 알려주는 화면이라 정보를 최소로 둔다.
 * 경보 1단계가 되면 같은 자리에서 10 귀가 안내로 바뀐다.
 * 시각 글자를 길게 누르면 11 데모 패널 (발표 시연용).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ElderHomeScreen(isLinked: Boolean, onOpenDemo: () -> Unit) {
    val context = LocalContext.current
    // TODO(코드A): 시뮬레이션 시계를 따르도록 교체. 지금은 기기 시각.
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { delay(10_000); now = LocalDateTime.now() } }
    val date = now.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN))
    val time = now.format(DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN))

    Column(
        Modifier.fillMaxSize().background(AdwdColors.Mint).statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            Modifier.combinedClickable(onClick = {}, onLongClick = onOpenDemo),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(date, style = AdwdType.ElderSub, color = AdwdColors.Slate)
            Text(time, style = AdwdType.ElderClock, color = AdwdColors.Purple)
        }

        Column(
            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp)).background(AdwdColors.White).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(132.dp).clip(CircleShape).background(AdwdColors.Teal), contentAlignment = Alignment.Center) {
                Icon(AdwdIcons.CheckCircle, contentDescription = null, tint = AdwdColors.Purple, modifier = Modifier.size(72.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text("이상한 점이 있으면\n알려드릴게요", style = AdwdType.ElderBody, color = AdwdColors.Purple, textAlign = TextAlign.Center)
        }

        // TODO: 연동된 보호자 번호로 바로 걸기 (민규 연동 모델에 번호가 생기면)
        ElderCallButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))) })

        LinkCodeCard(isLinked)
    }
}

/** 보호자가 04 화면에 입력할 6자리 코드 */
@Composable
private fun LinkCodeCard(isLinked: Boolean) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AdwdColors.White.copy(alpha = 0.7f))
            .drawBehind {
                drawRoundRect(
                    AdwdColors.Slate, cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))),
                )
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AdwdIcons.Link, contentDescription = null, tint = AdwdColors.Slate, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("보호자 연결 코드", style = AdwdType.BodySmall.copy(fontSize = 13.sp), color = AdwdColors.Slate)
            Text(FakeData.linkCodeSpaced, style = AdwdType.TitleLarge.copy(letterSpacing = 3.sp), color = AdwdColors.Purple)
        }
        if (isLinked) StatusChip("연결됨") else StatusChip("아직 연결 안 됨", container = AdwdColors.Canvas)
    }
}

