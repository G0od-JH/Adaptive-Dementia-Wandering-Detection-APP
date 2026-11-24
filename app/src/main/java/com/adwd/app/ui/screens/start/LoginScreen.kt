package com.adwd.app.ui.screens.start

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdCheckbox
import com.adwd.app.ui.components.AdwdLogo
import com.adwd.app.ui.components.AdwdTextField
import com.adwd.app.ui.components.PrimaryButton
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/**
 * 02 로그인. 보호자와 어르신 모두 각자 계정으로 로그인한다.
 * 계정은 앱 안(로컬 파일)에 저장한다. 서버가 없어서 아이디·비밀번호 찾기는 없다.
 */
@Composable
fun LoginScreen(onLogin: (firstLogin: Boolean) -> Unit, onSignup: () -> Unit) {
    var id by rememberSaveable { mutableStateOf(FakeData.guardianId) }
    var pw by rememberSaveable { mutableStateOf("") }
    var keep by rememberSaveable { mutableStateOf(true) }

    Column(
        Modifier
            .fillMaxSize()
            .background(AdwdColors.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 24.dp, end = 24.dp, top = 44.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AdwdLogo(size = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("ADWD", style = AdwdType.TitleLarge.copy(letterSpacing = 1.sp), color = AdwdColors.Purple)
                Text("로그인", style = AdwdType.BodySmall.copy(fontSize = 14.sp), color = AdwdColors.Slate)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            AdwdTextField(id, { id = it }, Modifier.fillMaxWidth(), label = "아이디")
            AdwdTextField(pw, { pw = it }, Modifier.fillMaxWidth(), label = "비밀번호", isPassword = true)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { keep = !keep }) {
                AdwdCheckbox(keep, { keep = it })
                Spacer(Modifier.width(8.dp))
                Text("로그인 상태 유지", style = AdwdType.BodySmall.copy(fontSize = 14.sp), color = AdwdColors.Slate)
            }
        }

        // TODO(민규 User 모델): 계정 확인. 처음 로그인한 계정이면 true → 03 사용자 선택·동의
        PrimaryButton("로그인", onClick = { onLogin(true) }, modifier = Modifier.fillMaxWidth(), enabled = id.isNotBlank())

        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("아직 계정이 없나요?", style = AdwdType.BodySmall.copy(fontSize = 14.sp), color = AdwdColors.Slate)
            Spacer(Modifier.width(16.dp))
            Text(
                "회원가입",
                style = AdwdType.BodySmallStrong.copy(fontSize = 14.sp, textDecoration = TextDecoration.Underline),
                color = AdwdColors.Purple,
                modifier = Modifier.clickable(onClick = onSignup),
            )
        }
        Spacer(Modifier.height(0.dp))
    }
}
