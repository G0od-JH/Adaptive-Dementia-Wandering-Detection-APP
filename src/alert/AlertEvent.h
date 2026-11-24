#pragma once

#include <string>
#include <vector>

#include "model/AlertLevel.h"
#include "model/LocationPoint.h"

namespace wander {

// 경보 단계가 바뀔 때 리스너에게 전달되는 정보.
// reasons : 이상으로 판단된 규칙들의 이유 문장 (화면에 그대로 표시)
// location: 단계가 바뀐 순간의 위치 (실종 신고 정보·지도 표시용)
struct AlertEvent {
    SimTime time;
    AlertLevel from;
    AlertLevel to;
    std::vector<std::string> reasons;
    LocationPoint location;

    bool isEscalation() const { return isHigher(to, from); }
};

}  // namespace wander
