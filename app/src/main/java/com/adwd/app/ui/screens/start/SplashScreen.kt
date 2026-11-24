package com.adwd.app.ui.screens.start

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdLogo
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType
import kotlinx.coroutines.delay

/** 01 스플래시. 로그인 확인과 모의데이터 불러오는 동안 1~2초 보인다. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        // TODO(코드A): 모의데이터 로드가 끝나는 시점에 맞추기
        delay(1500)
        onFinished()
    }
    Box(Modifier.fillMaxSize().background(AdwdColors.Purple)) {
        Column(
            Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AdwdLogo(size = 128.dp, background = AdwdColors.Slate)
            Spacer(Modifier.height(26.dp))
            Text("ADWD", style = AdwdType.TitleLarge.copy(fontSize = 44.sp, letterSpacing = 2.6.sp), color = AdwdColors.White)
            Spacer(Modifier.height(8.dp))
            Text(
                "Adaptive Dementia Wandering Detection",
                style = AdwdType.Label.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
                color = AdwdColors.Teal,
            )
        }
        LoadingDots(Modifier.align(Alignment.BottomCenter).padding(bottom = 64.dp))
    }
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "dots")
    val phase by t.animateFloat(0f, 3f, infiniteRepeatable(tween(1200), RepeatMode.Restart), label = "phase")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(3) { i ->
            val on = phase.toInt() == i
            Box(
                Modifier.size(8.dp).clip(CircleShape)
                    .background(if (on) AdwdColors.Teal else AdwdColors.Mint.copy(alpha = 0.35f)),
            )
        }
    }
}
