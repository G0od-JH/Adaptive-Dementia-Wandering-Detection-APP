#pragma once

#include "rule/Rule.h"

namespace wander {

// 주요 동선에서 300m 이상 벗어난 상태가 10분 이상 지속 -> 이상. 구현: 코드B
class RouteDeviationRule : public Rule {
public:
    RouteDeviationRule() : Rule("경로 이탈") {}
    RuleResult evaluate(const Trajectory& outing, const PatternProfile& profile) const override;
};

}  // namespace wander
