package com.adwd.app.ui.screens.elder

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.AlertBadge
import com.adwd.app.ui.components.ElderCallButton
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/** 다음 갈 방향. 코드A(경로)가 주는 값으로 바꾼다. */
data class GuideStep(
    val direction: String = "왼쪽으로 가세요",
    /** 화살표 각도. 0 = 위(직진), -90 = 왼쪽, 90 = 오른쪽 */
    val arrowDegrees: Float = -90f,
    val remain: String = "집까지 420m · 약 7분",
    val hint: String = "시장 앞 사거리에서 왼쪽",
)

/**
 * 10 / 10-2 / 10-3 어르신 귀가 안내. 단계마다 바탕색, 원 색, 화살표 색, 배지만 바뀐다.
 * "괜찮아요, 산책 중이에요"를 누르면 경보가 풀리고 09 홈으로 돌아간다.
 */
@Composable
fun ElderGuideScreen(level: AlertLevel, onImOkay: () -> Unit, step: GuideStep = GuideStep()) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().background(level.elderBackground).statusBarsPadding().navigationBarsPadding()
            .padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 24.dp),
    ) {
        AlertBadge(level)
        Spacer(Modifier.height(11.dp))
        Text("집으로 가는 길을\n알려드릴게요", style = AdwdType.ElderDisplay, color = AdwdColors.Purple)
        Spacer(Modifier.height(20.dp))

        Column(
            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(24.dp)).background(AdwdColors.White).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(Modifier.size(150.dp).clip(CircleShape).background(level.guideCircle), contentAlignment = Alignment.Center) {
                Icon(
                    AdwdIcons.DirectionArrow, contentDescription = step.direction, tint = level.guideArrow,
                    modifier = Modifier.size(84.dp).rotate(step.arrowDegrees),
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(step.direction, style = AdwdType.ElderDirection, color = AdwdColors.Purple)
            Spacer(Modifier.height(14.dp))
            Text(step.remain, style = AdwdType.ElderSub, color = AdwdColors.Slate)
            Spacer(Modifier.height(14.dp))
            Text(step.hint, style = AdwdType.BodyLarge, color = AdwdColors.Slate)
        }

        Spacer(Modifier.height(18.dp))
        ElderCallButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))) })
        Spacer(Modifier.height(24.dp))
        Text(
            "괜찮아요, 산책 중이에요",
            style = AdwdType.TitleSection.copy(textDecoration = TextDecoration.Underline, fontSize = 18.sp),
            color = AdwdColors.Slate,
            modifier = Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(8.dp)).clickable(onClick = onImOkay).padding(8.dp),
        )
    }
}
