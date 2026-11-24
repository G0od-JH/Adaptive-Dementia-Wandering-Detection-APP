package com.adwd.app.ui.screens.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdCheckbox
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.AdwdLogo
import com.adwd.app.ui.components.PrimaryButton
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType
import com.adwd.app.ui.state.UserRole

/** 03 사용자 선택 · 위치정보 동의. 처음 로그인한 계정만 본다. */
@Composable
fun OnboardingScreen(onStart: (UserRole) -> Unit) {
    var role by rememberSaveable { mutableStateOf(UserRole.GUARDIAN) }
    var agreed by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(AdwdColors.Mint)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 32.dp),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            AdwdLogo(size = 72.dp)
            Spacer(Modifier.height(12.dp))
            Text("ADWD", style = AdwdType.TitleLarge.copy(fontSize = 28.sp, letterSpacing = 1.sp), color = AdwdColors.Purple)
        }
        Spacer(Modifier.height(36.dp))
        Text("누가 사용하나요?", style = AdwdType.BodySmallStrong.copy(fontSize = 14.sp), color = AdwdColors.Purple)
        Spacer(Modifier.height(12.dp))
        RoleCard("보호자로 시작", AdwdIcons.Bell, role == UserRole.GUARDIAN) { role = UserRole.GUARDIAN }
        Spacer(Modifier.height(10.dp))
        RoleCard("어르신으로 시작", AdwdIcons.Person, role == UserRole.ELDER) { role = UserRole.ELDER }
        Spacer(Modifier.height(28.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AdwdColors.White)
                .clickable { agreed = !agreed }
                .padding(16.dp),
        ) {
            AdwdCheckbox(agreed, { agreed = it })
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("(필수) 위치정보 수집·이용 동의", style = AdwdType.BodySmallStrong, color = AdwdColors.Purple)
                Text(
                    "평소 동선을 배우기 위해 위치 기록을 30일간 저장합니다. 보호자에게만 공유됩니다.",
                    style = AdwdType.BodySmall, color = AdwdColors.Slate,
                )
            }
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton("시작하기", onClick = { onStart(role) }, modifier = Modifier.fillMaxWidth(), enabled = agreed)
    }
}

@Composable
private fun RoleCard(text: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(shape)
            .background(AdwdColors.White)
            .border(if (selected) 2.dp else 1.dp, if (selected) AdwdColors.Purple else AdwdColors.Line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = AdwdColors.Purple, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(14.dp))
        Text(text, style = AdwdType.TitleSection, color = AdwdColors.Purple)
    }
}
