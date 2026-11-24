#pragma once

#include "rule/Rule.h"

namespace wander {

// 평소보다 긴 외출 (평소 외출 시간 x 1.5 초과) -> 이상. 구현: 코드B
class LongOutingRule : public Rule {
public:
    LongOutingRule() : Rule("긴 외출") {}
    RuleResult evaluate(const Trajectory& outing, const PatternProfile& profile) const override;
};

}  // namespace wander
