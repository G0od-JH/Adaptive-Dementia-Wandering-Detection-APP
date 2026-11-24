#pragma once

#include "alert/AlertEvent.h"

namespace wander {

// 경보 단계 변화 구독자 (Observer 패턴, 추상 클래스). AlertEngine::addListener로 등록.
// 예상 구현체:
//   - ElderGuideScreen       (지후)  Caution 이상 -> 큰 글씨·음성 귀가 안내
//   - GuardianScreen         (지후)  Warning 이상 -> 보호자 알림 표시
//   - MissingReportGenerator (코드A) Emergency -> 사진·최근 위치 기록으로 신고 정보 생성
// 주의
//   - 단계가 "바뀔 때만" 호출됨 (매 위치마다 X). 오래 걸리는 작업 넣지 말 것.
//   - 엔진은 리스너를 소유하지 않음(포인터만 보관). 리스너가 먼저 없어지면 removeListener 먼저!
class AlertListener {
public:
    virtual ~AlertListener() = default;
    virtual void onAlertChanged(const AlertEvent& event) = 0;
};

}  // namespace wander
