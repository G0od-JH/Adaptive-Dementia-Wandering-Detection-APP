package com.adwd.app.ui.screens.guardian

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.adwd.app.ui.components.AdwdIcons
import com.adwd.app.ui.components.AdwdTextField
import com.adwd.app.ui.components.BackHeader
import com.adwd.app.ui.components.FilterChip
import com.adwd.app.ui.components.MapVariant
import com.adwd.app.ui.components.MockMap
import com.adwd.app.ui.components.PrimaryButton
import com.adwd.app.ui.components.SecondaryButton
import com.adwd.app.ui.components.StatusChip
import com.adwd.app.ui.model.Place
import com.adwd.app.ui.model.PlaceType
import com.adwd.app.ui.model.VisitTime
import com.adwd.app.ui.theme.AdwdColors
import com.adwd.app.ui.theme.AdwdType

/** 04-1을 어디서 열었는지: 연동 직후 처음 입력 / 설정에서 관리 */
enum class PlacesMode { SETUP, MANAGE }

/**
 * 04-1 자주 가는 장소 등록 (콜드 스타트 대비).
 * 연동이 끝나면 보호자가 어르신의 집과 자주 가는 곳을 적는다. 동선을 배우는 처음 7일 동안
 * 코드B가 이 장소들을 "평소 가는 곳"으로 본다. 장소는 원하는 만큼 추가·삭제할 수 있고, 집은 꼭 하나 있어야 한다.
 */
@Composable
fun PlacesScreen(
    mode: PlacesMode,
    places: List<Place>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Place) -> Unit,
    onRemove: (Place) -> Unit,
    onDone: () -> Unit,
) {
    val hasHome = places.any { it.isHome }
    Column(
        Modifier.fillMaxSize().background(AdwdColors.White).statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp),
    ) {
        BackHeader(if (mode == PlacesMode.MANAGE) "자주 가는 장소" else null, onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            if (mode == PlacesMode.SETUP) {
                StatusChip("연결 완료 · 마지막 단계", container = AdwdColors.Mint)
                Spacer(Modifier.height(10.dp))
                Text("어르신이 자주 가는\n장소를 알려주세요", style = AdwdType.TitleLarge, color = AdwdColors.Purple)
                Spacer(Modifier.height(8.dp))
            }
            Text(
                "처음 7일은 평소 동선을 배우는 기간이에요. 그동안은 여기 적은 장소를 기준으로 판단해요.",
                style = AdwdType.BodySmall, color = AdwdColors.Slate,
            )
            Spacer(Modifier.height(14.dp))
            PlacesMap(places, Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)))
            Spacer(Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                places.forEach { place ->
                    PlaceCard(place, onClick = { onEdit(place) }, onRemove = { onRemove(place) })
                }
                if (!hasHome) {
                    Text("집 위치를 먼저 넣어주세요", style = AdwdType.BodySmallStrong, color = AdwdColors.Level3)
                }
                Row(
                    Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, AdwdColors.Teal, RoundedCornerShape(14.dp))
                        .clickable(onClick = onAdd),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(AdwdIcons.Plus, contentDescription = null, tint = AdwdColors.Slate, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("장소 추가", style = AdwdType.BodyStrong, color = AdwdColors.Slate)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        if (mode == PlacesMode.SETUP) {
            PrimaryButton(
                "완료 · ${places.size}곳 저장", onClick = onDone, enabled = hasHome,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PlaceCard(place: Place, onClick: () -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(14.dp)).background(AdwdColors.Card)
            .clickable(onClick = onClick).padding(start = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(if (place.isHome) AdwdColors.Purple else AdwdColors.Mint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(place.type.icon, contentDescription = null, tint = if (place.isHome) AdwdColors.White else AdwdColors.Purple, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(place.name, style = AdwdType.BodyStrong, color = AdwdColors.Purple)
            val sub = buildList {
                add(place.type.label)
                if (place.times.isNotEmpty()) add(VisitTime.entries.filter { it in place.times }.joinToString("·") { it.label })
                add(place.addressLabel)
            }.joinToString(" · ")
            Text(sub, style = AdwdType.Caption, color = AdwdColors.Slate)
        }
        if (place.isHome) {
            Text("필수", style = AdwdType.LabelStrong, color = AdwdColors.Slate, modifier = Modifier.padding(end = 12.dp))
        } else {
            Box(Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onRemove), contentAlignment = Alignment.Center) {
                Icon(AdwdIcons.Trash, contentDescription = "${place.name} 삭제", tint = AdwdColors.Slate, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * 모의 지도 위에 장소 핀을 찍는다. 지도 비율을 350:260으로 고정해서 x, y(0~1)가 그대로 맞는다.
 * TODO(코드A): 실제 지도 패널로 교체
 */
@Composable
private fun PlacesMap(
    places: List<Place>,
    modifier: Modifier = Modifier,
    highlightId: Int? = null,
    onTap: ((Float, Float) -> Unit)? = null,
) {
    BoxWithConstraints(
        modifier.aspectRatio(MapVariant.HOME.viewW / MapVariant.HOME.viewH)
            .let { m ->
                if (onTap == null) m else m.pointerInput(Unit) {
                    detectTapGestures { o -> onTap(o.x / size.width, o.y / size.height) }
                }
            },
    ) {
        MockMap(MapVariant.HOME, Modifier.fillMaxSize(), showMarkers = false)
        places.forEach { p ->
            val strong = p.isHome || p.id == highlightId
            Box(
                Modifier.offset(x = maxWidth * p.x - 16.dp, y = maxHeight * p.y - 16.dp).size(32.dp)
                    .clip(CircleShape).background(AdwdColors.White).padding(3.dp)
                    .clip(CircleShape).background(if (strong) AdwdColors.Purple else AdwdColors.Teal),
                contentAlignment = Alignment.Center,
            ) {
                Icon(p.type.icon, contentDescription = p.name, tint = if (strong) AdwdColors.White else AdwdColors.Purple, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * 04-2 장소 추가·수정 / 08-1 어르신 집 위치.
 * 지도를 눌러 위치를 찍고, 이름·종류·주로 가는 시간대를 고른다. 집 모드에서는 위치만 정한다.
 * @param initial 고칠 장소. null이면 새 장소.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlaceEditScreen(
    initial: Place?,
    homeOnly: Boolean,
    others: List<Place>,
    onBack: () -> Unit,
    onSave: (Place) -> Unit,
    onDelete: (Place) -> Unit,
) {
    val isHome = homeOnly || initial?.isHome == true
    var name by rememberSaveable { mutableStateOf(initial?.name ?: if (isHome) "집" else "") }
    var type by rememberSaveable { mutableStateOf(initial?.type ?: if (isHome) PlaceType.HOME else PlaceType.SENIOR_CENTER) }
    var times by rememberSaveable { mutableStateOf(initial?.times ?: emptySet()) }
    var x by rememberSaveable { mutableFloatStateOf(initial?.x ?: 0.5f) }
    var y by rememberSaveable { mutableFloatStateOf(initial?.y ?: 0.5f) }
    var picked by rememberSaveable { mutableStateOf(initial != null) }

    val draft = Place(initial?.id ?: -1, name.trim(), type, x, y, times)
    val title = when {
        isHome -> "어르신 집 위치"
        initial == null -> "장소 추가"
        else -> "장소 수정"
    }

    Column(
        Modifier.fillMaxSize().background(AdwdColors.White).statusBarsPadding().navigationBarsPadding().imePadding().padding(horizontal = 18.dp),
    ) {
        BackHeader(title, onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(4.dp))
            Text(
                if (isHome) "지도를 눌러 어르신 집을 찍어주세요. 귀가 안내 방향과 경보 판단의 기준이 돼요."
                else "지도를 눌러 장소 위치를 찍어주세요.",
                style = AdwdType.BodySmall, color = AdwdColors.Slate,
            )
            Spacer(Modifier.height(10.dp))
            PlacesMap(
                places = others.filterNot { it.id == initial?.id } + if (picked) listOf(draft) else emptyList(),
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),
                highlightId = draft.id,
                onTap = { tx, ty -> x = tx.coerceIn(0.04f, 0.96f); y = ty.coerceIn(0.05f, 0.95f); picked = true },
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(AdwdIcons.Pin, contentDescription = null, tint = AdwdColors.Slate, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (picked) draft.addressLabel else "아직 위치를 찍지 않았어요", style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
            }

            if (isHome) {
                Spacer(Modifier.height(14.dp))
                Text(
                    "집은 꼭 하나만 둘 수 있어요. 새로 찍으면 기존 위치가 바뀌어요.",
                    style = AdwdType.BodySmall, color = AdwdColors.Slate,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AdwdColors.Card).padding(14.dp),
                )
            } else {
                Spacer(Modifier.height(18.dp))
                AdwdTextField(name, { name = it.take(20) }, Modifier.fillMaxWidth(), label = "장소 이름")
                Spacer(Modifier.height(16.dp))
                Text("어떤 곳인가요", style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlaceType.entries.filter { it != PlaceType.HOME }.forEach { t ->
                        FilterChip(t.label, selected = type == t, onClick = { type = t })
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("주로 가는 시간대 (여러 개 선택)", style = AdwdType.BodySmallStrong, color = AdwdColors.Slate)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    VisitTime.entries.forEach { t ->
                        FilterChip(t.label, selected = t in times, onClick = { times = if (t in times) times - t else times + t })
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        val canSave = picked && draft.name.isNotEmpty()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (initial != null && !isHome) {
                SecondaryButton("삭제", onClick = { onDelete(initial) }, content = AdwdColors.Level3, modifier = Modifier.weight(1f))
            }
            PrimaryButton("저장", onClick = { onSave(draft) }, enabled = canSave, modifier = Modifier.weight(2f))
        }
        Spacer(Modifier.height(16.dp))
    }
}
