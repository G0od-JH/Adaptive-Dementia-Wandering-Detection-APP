#pragma once

#include <memory>

#include "model/AlertLevel.h"
#include "model/SimTime.h"

namespace wander {

// 경보 단계 (State 패턴, 추상 클래스). 구현체 4개: Normal / Caution / Warning / Emergency (AlertStates.h)
//
// 전이표 (초안, 시간 기준은 Thresholds.h, 최초 이상 감지부터 누적):
//   Normal    --이상 감지-->                         Caution   (본인에게 귀가 안내)
//   Caution   --정상 복귀--> Normal,  --이상 10분 지속--> Warning   (보호자 알림)
//   Warning   --정상 복귀--> Normal,  --이상 30분 지속--> Emergency (실종 신고 정보 생성)
//   Emergency --보호자 해제(AlertEngine::resolve)만--> Normal
class AlertState {
public:
    virtual ~AlertState() = default;

    virtual AlertLevel level() const = 0;

    // abnormal   : 이번 판단에서 이상 규칙이 1개 이상인지
    // abnormalFor: 이상 상태가 연속으로 지속된 시간(초). 정상이면 0
    // 반환: 다음 상태. 지금 상태 그대로면 nullptr
    virtual std::unique_ptr<AlertState> next(bool abnormal, Seconds abnormalFor) const = 0;
};

}  // namespace wander
