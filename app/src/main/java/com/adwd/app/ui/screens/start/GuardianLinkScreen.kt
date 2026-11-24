package com.adwd.app.ui.screens.start

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.BackHeader
import com.adwd.app.ui.components.PrimaryButton
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/** 04 어르신 연동. 보호자가 어르신 앱의 6자리 연결 코드를 입력한다. */
@Composable
fun GuardianLinkScreen(onBack: () -> Unit, onLinked: () -> Unit) {
    var code by rememberSaveable { mutableStateOf("") }
    // TODO(민규 연동 모델): 코드로 어르신 계정 찾기. 지금은 FakeData 코드와 같으면 찾은 것으로 본다.
    val found = code.length == 6 && code == FakeData.linkCode

    Column(
        Modifier
            .fillMaxSize()
            .background(AdwdColors.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 22.dp)
            .padding(top = 4.dp, bottom = 36.dp),
    ) {
        BackHeader(null, onBack)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(AdwdColors.Mint), contentAlignment = Alignment.Center) {
            Icon(AdwdIcons.Link, contentDescription = null, tint = AdwdColors.Purple, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text("어르신과 연결할게요", style = AdwdType.TitleLarge, color = AdwdColors.Purple)
        Spacer(Modifier.height(8.dp))
        Text("어르신 앱 첫 화면 아래의 연결 코드 6자리를 입력하세요.", style = AdwdType.BodyRegular, color = AdwdColors.Slate)
        Spacer(Modifier.height(20.dp))
        Text("연결 코드", style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
        Spacer(Modifier.height(10.dp))
        CodeInput(code, onChange = { code = it.filter(Char::isDigit).take(6) })

        if (code.length == 6) {
            Spacer(Modifier.height(26.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (found) AdwdColors.Mint else AdwdColors.DangerBg).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(AdwdColors.White), contentAlignment = Alignment.Center) {
                    Icon(AdwdIcons.Person, contentDescription = null, tint = AdwdColors.Slate, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(if (found) "${FakeData.elderId} 님 계정을 찾았어요" else "이 코드의 계정을 찾지 못했어요", style = AdwdType.BodyStrong, color = AdwdColors.Purple)
                    Text(if (found) "맞으면 연결하기를 눌러주세요" else "어르신 화면의 코드를 다시 확인해 주세요", style = AdwdType.BodySmall, color = AdwdColors.Slate)
                }
            }
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton("연결하기", onClick = onLinked, modifier = Modifier.fillMaxWidth(), enabled = found)
    }
}

/** 6칸 코드 입력. 숨은 입력칸 하나에 칸 모양만 그린다. */
@Composable
private fun CodeInput(code: String, onChange: (String) -> Unit) {
    BasicTextField(
        value = code,
        onValueChange = onChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = { inner ->
            // 실제 입력칸은 보이지 않게 두고(포커스·입력용), 칸 모양만 그린다
            Box(Modifier.size(1.dp).alpha(0f)) { inner() }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                repeat(6) { i ->
                    val ch = code.getOrNull(i)
                    val active = i == code.length
                    Box(
                        Modifier
                            .size(width = 44.dp, height = 58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AdwdColors.White)
                            .border(if (active) 2.dp else 1.5.dp, if (active) AdwdColors.Purple else AdwdColors.InputLine, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(ch?.toString() ?: if (active) "_" else "", style = AdwdType.TitleLarge, color = AdwdColors.Purple)
                    }
                }
            }
        },
    )
}
