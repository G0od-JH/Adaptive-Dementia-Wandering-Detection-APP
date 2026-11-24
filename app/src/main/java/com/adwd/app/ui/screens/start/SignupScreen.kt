package com.adwd.app.ui.screens.start

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdTextField
import com.adwd.app.ui.components.BackHeader
import com.adwd.app.ui.components.PrimaryButton
import com.adwd.app.ui.components.SecondaryButton
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/** 02-1 회원가입. 아이디 중복 확인 + 비밀번호 확인. 로컬 저장이라 이메일 인증은 없다. */
@Composable
fun SignupScreen(onBack: () -> Unit, onDone: () -> Unit) {
    var id by rememberSaveable { mutableStateOf("") }
    var pw by rememberSaveable { mutableStateOf("") }
    var pw2 by rememberSaveable { mutableStateOf("") }
    var idChecked by rememberSaveable { mutableStateOf(false) }
    val pwMatch = pw.isNotEmpty() && pw == pw2
    val okStyle = AdwdType.Label.copy(fontSize = 12.5.sp)

    Column(
        Modifier
            .fillMaxSize()
            .background(AdwdColors.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(top = 4.dp, bottom = 36.dp),
    ) {
        BackHeader("회원가입", onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(Modifier.height(16.dp))
            Text("ADWD에서 사용할\n아이디를 만들어요", style = AdwdType.TitleLarge, color = AdwdColors.Purple)
            Spacer(Modifier.height(16.dp))

            Text("아이디", style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
            Row(verticalAlignment = Alignment.CenterVertically) {
                AdwdTextField(id, { id = it; idChecked = false }, Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                // TODO(민규 User 모델): 저장된 계정 목록에서 중복 확인
                SecondaryButton("중복 확인", onClick = { idChecked = id.isNotBlank() }, modifier = Modifier.width(82.dp), height = 52.dp, textStyle = AdwdType.BodySmallStrong.copy(fontSize = 14.sp))
            }
            if (idChecked) Text("사용할 수 있는 아이디예요", style = okStyle, color = AdwdColors.Slate)

            Spacer(Modifier.height(8.dp))
            AdwdTextField(pw, { pw = it }, Modifier.fillMaxWidth(), label = "비밀번호", isPassword = true)
            Spacer(Modifier.height(8.dp))
            AdwdTextField(pw2, { pw2 = it }, Modifier.fillMaxWidth(), label = "비밀번호 확인", isPassword = true)
            if (pw2.isNotEmpty()) {
                Text(if (pwMatch) "비밀번호가 같아요" else "비밀번호가 달라요", style = okStyle, color = if (pwMatch) AdwdColors.Slate else AdwdColors.Level3)
            }
        }
        PrimaryButton("가입하기", onClick = onDone, modifier = Modifier.fillMaxWidth(), enabled = idChecked && pwMatch)
    }
}
