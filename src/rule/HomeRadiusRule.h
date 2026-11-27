#pragma once

#include "rule/Rule.h"

namespace wander {

// 콜드 스타트 전용: 집·등록 장소 중 가장 가까운 곳에서 1km 넘게 떨어지면 이상 (학습 전이라 고정 반경). 구현: 코드B
class HomeRadiusRule : public Rule {
public:
    HomeRadiusRule() : Rule("집 반경 초과") {}
    RuleResult evaluate(const Trajectory& outing, const PatternProfile& profile) const override;
};

}  // namespace wander
