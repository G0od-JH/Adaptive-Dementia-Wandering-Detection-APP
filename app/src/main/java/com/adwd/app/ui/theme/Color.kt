package com.adwd.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 피그마 Foundation 페이지의 컬러 스타일과 1:1로 맞춘 색.
 * 새 색이 필요하면 피그마 스타일을 먼저 만들고 여기에 같은 이름으로 추가한다.
 */
object AdwdColors {
    // Brand (받은 팔레트 4색)
    val Purple = Color(0xFF321E48) // 제목·본문 글자, 주요 버튼
    val Slate = Color(0xFF43637E)  // 보조 글자, 아이콘, 비활성 탭
    val Teal = Color(0xFF65DCD5)   // 정상 상태, 강조 면 (글자색으로는 쓰지 않음)
    val Mint = Color(0xFFD9FFF4)   // 카드 배경, 어르신 화면 바탕

    // Alert (경보 단계 색, 팔레트와 별도)
    val Level0 = Teal
    val Level1 = Color(0xFFFFC857)
    val Level2 = Color(0xFFFF8A3D)
    val Level3 = Color(0xFFC93439)

    // Surface
    val White = Color(0xFFFFFFFF)
    val Canvas = Color(0xFFEEF3F5)
    val Card = Color(0xFFF4FAF9)
    val Line = Color(0xFFD5E0E6)
    val InputLine = Color(0xFFC9D7DE)
    val TabLine = Color(0xFFE3ECEF)
    val WarnBg = Color(0xFFFFE9DA)
    val DangerBg = Color(0xFFFBE1E2)

    // 지도 자리(모의 지도)
    val MapBg = Color(0xFFE9F4F2)
    val MapBlock = Color(0xFFD7E6E8)
    val MapPark = Color(0xFFC4ECDD)
}
