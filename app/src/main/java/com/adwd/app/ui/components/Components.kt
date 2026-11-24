package com.adwd.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

// ─────────────────────────────────────────────────────────────
// 버튼 (피그마 Button/Primary, Button/Secondary, Button/Elder Call)
// ─────────────────────────────────────────────────────────────

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    container: Color = AdwdColors.Purple,
    content: Color = AdwdColors.White,
    height: Dp = 56.dp,
    textStyle: TextStyle = AdwdType.ButtonText,
) {
    Row(
        modifier
            .height(height)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) container else container.copy(alpha = 0.35f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = textStyle, color = content)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: Color = AdwdColors.Purple,
    height: Dp = 56.dp,
    textStyle: TextStyle = AdwdType.ButtonText,
) {
    Box(
        modifier
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(AdwdColors.White)
            .border(1.5.dp, AdwdColors.InputLine, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = textStyle, color = content) }
}

/** 어르신 화면의 큰 "보호자에게 전화" 버튼 (높이 72) */
@Composable
fun ElderCallButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AdwdColors.Purple)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AdwdIcons.Phone, contentDescription = null, tint = AdwdColors.White, modifier = Modifier.size(30.dp))
        Spacer(Modifier.width(12.dp))
        Text("보호자에게 전화", style = AdwdType.ElderButton, color = AdwdColors.White)
    }
}

// ─────────────────────────────────────────────────────────────
// 경보 표시 (피그마 Alert/Badge, Alert/Dot)
// ─────────────────────────────────────────────────────────────

/** "1 주의" 처럼 아이콘 + 숫자 + 이름이 들어간 알약 모양 배지 */
@Composable
fun AlertBadge(level: AlertLevel, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(level.color)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(level.icon, contentDescription = null, tint = level.onColor, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(level.title, style = AdwdType.BodySmallStrong.copy(fontSize = 14.sp), color = level.onColor)
    }
}

/** 숫자만 들어간 작은 네모 (설정 화면 등) */
@Composable
fun LevelDot(level: AlertLevel, modifier: Modifier = Modifier, size: Dp = 22.dp) {
    Box(
        modifier.size(size).clip(RoundedCornerShape(6.dp)).background(level.color),
        contentAlignment = Alignment.Center,
    ) { Text(level.step.toString(), style = AdwdType.TabActive, color = level.onColor) }
}

/** 단계 색 네모 안에 단계 아이콘 (알림 목록, 디자인 가이드) */
@Composable
fun LevelIconBox(level: AlertLevel, modifier: Modifier = Modifier, size: Dp = 38.dp, iconSize: Dp = 20.dp) {
    Box(
        modifier.size(size).clip(RoundedCornerShape(11.dp)).background(level.color),
        contentAlignment = Alignment.Center,
    ) { Icon(level.icon, contentDescription = level.title, tint = level.onColor, modifier = Modifier.size(iconSize)) }
}

/** "연결됨" 같은 상태 칩 */
@Composable
fun StatusChip(text: String, modifier: Modifier = Modifier, container: Color = AdwdColors.Teal) {
    Box(
        modifier.clip(RoundedCornerShape(999.dp)).background(container).padding(horizontal = 10.dp, vertical = 4.dp),
    ) { Text(text, style = AdwdType.BodySmallStrong, color = AdwdColors.Purple) }
}

/** 알림 목록의 필터 칩 */
@Composable
fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(if (selected) AdwdColors.Purple else AdwdColors.White)
            .border(1.dp, if (selected) AdwdColors.Purple else AdwdColors.Line, RoundedCornerShape(17.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, style = AdwdType.BodySmallStrong, color = if (selected) AdwdColors.White else AdwdColors.Slate) }
}

// ─────────────────────────────────────────────────────────────
// 입력 (로그인, 회원가입)
// ─────────────────────────────────────────────────────────────

@Composable
fun AdwdTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (label != null) Text(label, style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = AdwdType.BodyLarge.copy(color = AdwdColors.Purple),
            cursorBrush = SolidColor(AdwdColors.Purple),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            decorationBox = { inner ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AdwdColors.White)
                        .border(1.5.dp, AdwdColors.InputLine, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart,
                ) { inner() }
            },
        )
    }
}

/** 둥근 보라색 체크박스 (피그마 checkbox) */
@Composable
fun AdwdCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(20.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(if (checked) AdwdColors.Purple else AdwdColors.White)
            .border(1.5.dp, if (checked) AdwdColors.Purple else AdwdColors.InputLine, RoundedCornerShape(5.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) Icon(Check, contentDescription = "선택됨", tint = AdwdColors.White, modifier = Modifier.size(16.dp))
    }
}

private val Check: ImageVector = androidx.compose.ui.graphics.vector.ImageVector.Builder("check", 24.dp, 24.dp, 24f, 24f).apply {
    addPath(
        pathData = androidx.compose.ui.graphics.vector.addPathNodes("M6 12.6l3.8 3.8L18 8.4"),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2.6f,
        strokeLineCap = androidx.compose.ui.graphics.StrokeCap.Round,
        strokeLineJoin = androidx.compose.ui.graphics.StrokeJoin.Round,
    )
}.build()

// ─────────────────────────────────────────────────────────────
// 머리줄, 카드
// ─────────────────────────────────────────────────────────────

/** ‹ 뒤로 + 제목 */
@Composable
fun BackHeader(title: String?, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().height(48.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).clickable(onClick = onBack), contentAlignment = Alignment.CenterStart) {
            Icon(AdwdIcons.Back, contentDescription = "뒤로", tint = AdwdColors.Purple, modifier = Modifier.size(24.dp))
        }
        if (title != null) Text(title, style = AdwdType.TitleSection, color = AdwdColors.Purple)
    }
}

/** 흰 카드 + 얇은 테두리 (피그마 Card/Base) */
fun Modifier.adwdCard(
    background: Color = AdwdColors.White,
    border: Color? = AdwdColors.Line,
    radius: Dp = 16.dp,
): Modifier {
    val shape = RoundedCornerShape(radius)
    return this.clip(shape).background(background).let { if (border != null) it.border(BorderStroke(1.dp, border), shape) else it }
}

// ─────────────────────────────────────────────────────────────
// 보호자 탭바 (피그마 Nav/Tab bar)
// ─────────────────────────────────────────────────────────────

enum class GuardianTab(val label: String, val icon: ImageVector) {
    HOME("홈", AdwdIcons.Map),
    ALERTS("알림", AdwdIcons.Bell),
    SETTINGS("설정", AdwdIcons.Settings),
}

@Composable
fun GuardianTabBar(selected: GuardianTab, onSelect: (GuardianTab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(AdwdColors.White)
            .drawBehind { drawLine(AdwdColors.TabLine, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
            .navigationBarsPadding()
            .padding(top = 13.dp, bottom = 12.dp),
    ) {
        GuardianTab.entries.forEach { tab ->
            val on = tab == selected
            val color = if (on) AdwdColors.Purple else AdwdColors.Slate
            Column(
                Modifier.weight(1f).clickable { onSelect(tab) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(tab.icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                Text(tab.label, style = if (on) AdwdType.TabActive else AdwdType.Tab, color = color)
            }
        }
    }
}

/** 화면 좌우 기본 여백 */
val ScreenPadding = PaddingValues(horizontal = 18.dp)
