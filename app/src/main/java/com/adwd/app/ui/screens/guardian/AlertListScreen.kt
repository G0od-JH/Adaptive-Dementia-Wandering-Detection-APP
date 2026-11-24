package com.adwd.app.ui.screens.guardian

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.FilterChip
import com.adwd.app.ui.components.GuardianTab
import com.adwd.app.ui.components.LevelIconBox
import com.adwd.app.ui.model.AlertLevel
import com.adwd.app.ui.model.FakeData
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/** 05-1 알림 목록(경보 기록). 지금 상태 + 단계별 필터 + 날짜별 기록. 기록을 누르면 06 경보 상세. */
@Composable
fun AlertListScreen(
    currentLevel: AlertLevel,
    onOpen: (id: Int) -> Unit,
    onTab: (GuardianTab) -> Unit,
    records: List<FakeData.AlertRecord> = FakeData.alertRecords,
) {
    var filter by rememberSaveable { mutableStateOf<Int?>(null) }
    val shown = records.filter { filter == null || it.level.step == filter }

    GuardianScaffold(GuardianTab.ALERTS, onTab) {
        Spacer(Modifier.height(8.dp))
        Text("알림", style = AdwdType.TitleScreen, color = AdwdColors.Purple)
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(14.dp)).background(currentLevel.color).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(currentLevel.icon, contentDescription = null, tint = currentLevel.onColor, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(10.dp))
            Text("지금은 ${currentLevel.title}이에요", style = AdwdType.ButtonText, color = currentLevel.onColor)
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip("전체", filter == null, { filter = null })
            listOf(AlertLevel.CAUTION, AlertLevel.WARNING, AlertLevel.DANGER).forEach { l ->
                FilterChip(l.title, filter == l.step, { filter = l.step })
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            shown.groupBy { it.dateLabel }.forEach { (date, list) ->
                item(key = "h_$date") {
                    Text(date, style = AdwdType.LabelStrong, color = AdwdColors.Slate, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
                }
                items(list, key = { it.id }) { r -> RecordRow(r) { onOpen(r.id) } }
            }
            if (shown.isEmpty()) item {
                Text("이 단계의 기록이 없어요", style = AdwdType.BodySmall, color = AdwdColors.Slate, modifier = Modifier.padding(top = 24.dp))
            }
        }
    }
}

@Composable
private fun RecordRow(r: FakeData.AlertRecord, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LevelIconBox(r.level)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("${r.level.title} · ${r.reason}", style = AdwdType.BodyStrong, color = AdwdColors.Purple)
            Text(r.detail, style = AdwdType.Caption.copy(fontSize = 12.5.sp), color = AdwdColors.Slate)
        }
        Icon(AdwdIcons.ChevronRight, contentDescription = null, tint = AdwdColors.Slate, modifier = Modifier.size(18.dp))
    }
}
